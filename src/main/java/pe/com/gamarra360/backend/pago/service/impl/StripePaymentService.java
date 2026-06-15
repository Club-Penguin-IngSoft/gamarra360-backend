package pe.com.gamarra360.backend.pago.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.TransferCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.VarianteProductoService;
import pe.com.gamarra360.backend.enums.EstadoPago;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeRequest;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeResponse;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.entity.Pago;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import pe.com.gamarra360.backend.pago.repository.PagoRepository;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripePaymentService {

    private final OrdenPagoRepository   ordenPagoRepository;
    private final PedidoRepository      pedidoRepository;
    private final ComercianteRepository comercianteRepository;
    private final PagoRepository        pagoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    @Value("${stripe.commission-rate:0.10}")
    private double commissionRate;

    @Transactional
    public CrearPagoStripeResponse crearPaymentIntent(CrearPagoStripeRequest request)
            throws StripeException {

        OrdenPago orden = ordenPagoRepository.findById(request.ordenPagoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "OrdenPago no encontrada: " + request.ordenPagoId()));

        if (orden.getEstado() == EstadoPago.PAGADO) {
            throw new IllegalStateException("La orden ya fue pagada.");
        }

        // ── Calcula el total sumando los pedidos hijos ──────────────
        // orden.getTotal() puede llegar 0 si el frontend lo envió así.
        // Los pedidos hijos sí tienen el monto correcto por tienda.
        double totalReal = pedidoRepository.findByOrdenPagoId(orden.getId())
                .stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                .sum();

        log.info("OrdenPago {} — total en orden: S/ {} — total sumado de pedidos: S/ {}",
                orden.getId(), orden.getTotal(), totalReal);

        // Fallback: si los pedidos tampoco tienen monto, usa el de la orden
        if (totalReal <= 0 && orden.getTotal() != null && orden.getTotal() > 0) {
            totalReal = orden.getTotal();
        }

        if (totalReal <= 0) {
            log.error("OrdenPago {} no tiene monto válido ni en la orden ni en sus pedidos.", orden.getId());
            throw new IllegalArgumentException(
                    "La orden no tiene un monto válido. Verifica que los pedidos tengan total > 0.");
        }

        // Actualiza el total de la orden en BD con el valor real
        orden.setTotal(totalReal);
        ordenPagoRepository.save(orden);

        long totalCentimos = Math.round(totalReal * 100);

        String vendedorIds = pedidoRepository.findByOrdenPagoId(orden.getId())
                .stream()
                .map(p -> String.valueOf(p.getVendedorId()))
                .distinct()
                .collect(Collectors.joining(","));

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalCentimos)
                .setCurrency("pen")
                .setDescription("Compra Gamarra360 - Orden #" + orden.getId())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .putMetadata("orden_pago_id", String.valueOf(orden.getId()))
                .putMetadata("cliente_id",    String.valueOf(orden.getClienteId()))
                .putMetadata("vendedor_ids",  vendedorIds)
                .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("orden-" + orden.getId())
                .build();

        PaymentIntent intent = PaymentIntent.create(params, options);
        log.info("PaymentIntent creado: {} para OrdenPago: {} — S/ {}",
                intent.getId(), orden.getId(), totalReal);

        Pago pago = pagoRepository.findByOrdenPagoId(orden.getId())
                .orElse(new Pago());

        pago.setOrdenPagoId(orden.getId());
        pago.setMonto(totalReal);
        pago.setMetodo("STRIPE");
        pago.setStripePaymentIntentId(intent.getId());
        pago.setStripeClientSecret(intent.getClientSecret());
        pago.setEstado(EstadoPago.PENDIENTE);
        pagoRepository.save(pago);

        return new CrearPagoStripeResponse(
                pago.getId(),
                intent.getClientSecret(),
                totalCentimos,
                "pen"
        );
    }

    @Transactional
    public void procesarPagoConfirmado(PaymentIntent intent) throws StripeException {
        String ordenPagoId = intent.getMetadata().get("orden_pago_id");

        OrdenPago orden = ordenPagoRepository.findById(Long.valueOf(ordenPagoId))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "OrdenPago no encontrada: " + ordenPagoId));

        // Actualiza estado del pago si aún no está confirmado
        if (orden.getEstado() != EstadoPago.PAGADO) {
            orden.confirmarPago();
            ordenPagoRepository.save(orden);

            pagoRepository.findByStripePaymentIntentId(intent.getId())
                    .ifPresent(pago -> {
                        pago.confirmarPago();
                        pagoRepository.save(pago);
                    });
        } else {
            log.info("OrdenPago {} ya estaba PAGADA (marcada por frontend). " +
                    "Continuando con descuento de stock y transfers.", ordenPagoId);
        }

        // ── Descuento de stock y transfers SIEMPRE se ejecutan ──
        List<Pedido> pedidos = pedidoRepository.findByOrdenPagoId(orden.getId());

        for (Pedido pedido : pedidos) {
            List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedido.getId());

            for (DetallePedido detalle : detalles) {
                if (detalle.getIdVarianteProducto() == null) continue;

                VarianteProducto variante = varianteProductoRepository
                        .findByIdWithLock(detalle.getIdVarianteProducto())
                        .orElse(null);

                if (variante == null) {
                    log.warn("Variante {} no encontrada", detalle.getIdVarianteProducto());
                    continue;
                }

                int stockActual = variante.getStock() != null ? variante.getStock() : 0;
                int cantidad    = detalle.getCantidad() != null ? detalle.getCantidad() : 0;

                if (stockActual < cantidad) {
                    log.error("Stock insuficiente variante {} — disponible: {}, solicitado: {}",
                            variante.getIdVariante(), stockActual, cantidad);
                    emitirReembolso(intent, pedido, detalle);
                    continue;
                }

                int nuevoStock = stockActual - cantidad;
                variante.setStock(nuevoStock);
                variante.setDisponible(nuevoStock > 0);
                varianteProductoRepository.saveAndFlush(variante);

                log.info("Stock variante {} actualizado: {} → {}",
                        variante.getIdVariante(), stockActual, nuevoStock);
            }

            // Transfers a vendedores
            if (pedido.getVendedorId() == null) continue;
            comercianteRepository.findById(pedido.getVendedorId()).ifPresent(comerciante -> {
                if (comerciante.getStripeAccountId() == null) {
                    log.warn("Vendedor {} sin Stripe Account. Transfer omitido.", pedido.getVendedorId());
                    return;
                }

                long subtotalCentimos = Math.round(pedido.getTotal() * 100);
                long montoVendedor    = Math.round(subtotalCentimos * (1 - commissionRate));

                try {
                    TransferCreateParams transferParams = TransferCreateParams.builder()
                            .setAmount(montoVendedor)
                            .setCurrency("pen")
                            .setDestination(comerciante.getStripeAccountId())
                            .setSourceTransaction(intent.getLatestCharge())
                            .putMetadata("pedido_id",     String.valueOf(pedido.getId()))
                            .putMetadata("vendedor_id",   String.valueOf(pedido.getVendedorId()))
                            .putMetadata("orden_pago_id", ordenPagoId)
                            .build();

                    Transfer transfer = Transfer.create(transferParams);
                    log.info("Transfer {} creado para vendedor {} — S/ {}",
                            transfer.getId(), pedido.getVendedorId(), montoVendedor / 100.0);

                } catch (StripeException e) {
                    log.error("Error creando transfer para vendedor {}: {}",
                            pedido.getVendedorId(), e.getMessage());
                }
            });
        }

        log.info("OrdenPago {} procesada: stock reducido y transfers distribuidos.", ordenPagoId);
    }

    private void emitirReembolso(PaymentIntent intent, Pedido pedido, DetallePedido detalle) {
        try {
            long montoReembolso = Math.round(
                    (detalle.getPrecio() != null ? detalle.getPrecio() : 0.0) *
                            (detalle.getCantidad() != null ? detalle.getCantidad() : 0) * 100
            );

            if (montoReembolso <= 0) {
                log.warn("Monto de reembolso inválido para detalle {} — omitido", detalle.getId());
                return;
            }

            com.stripe.param.RefundCreateParams refundParams =
                    com.stripe.param.RefundCreateParams.builder()
                            .setCharge(intent.getLatestCharge())
                            .setAmount(montoReembolso)
                            .putMetadata("motivo",      "stock_insuficiente")
                            .putMetadata("pedido_id",   String.valueOf(pedido.getId()))
                            .putMetadata("detalle_id",  String.valueOf(detalle.getId()))
                            .putMetadata("variante_id", String.valueOf(detalle.getIdVarianteProducto()))
                            .build();

            com.stripe.model.Refund refund = com.stripe.model.Refund.create(refundParams);
            log.info("Reembolso {} creado — S/ {} para detalle {} (stock insuficiente)",
                    refund.getId(), montoReembolso / 100.0, detalle.getId());

        } catch (StripeException e) {
            log.error("Error creando reembolso para detalle {}: {}", detalle.getId(), e.getMessage());
        }

        // ── Limpieza en BD ──────────────────────────────────────────────
        try {
            // 1. Elimina todos los detalles del pedido
            List<DetallePedido> todosLosDetalles =
                    detallePedidoRepository.findByPedidoId(pedido.getId());
            detallePedidoRepository.deleteAll(todosLosDetalles);
            log.info("Detalles del pedido {} eliminados", pedido.getId());

            // 2. Elimina el pedido
            pedidoRepository.deleteById(pedido.getId());
            log.info("Pedido {} eliminado por stock insuficiente", pedido.getId());

            // 3. Marca la OrdenPago como FALLIDO
            ordenPagoRepository.findById(pedido.getOrdenPagoId()).ifPresent(orden -> {
                orden.cancelar();
                ordenPagoRepository.save(orden);
                log.info("OrdenPago {} marcada como FALLIDO", orden.getId());
            });

        } catch (Exception e) {
            log.error("Error limpiando BD tras stock insuficiente en pedido {}: {}",
                    pedido.getId(), e.getMessage());
        }
    }
}