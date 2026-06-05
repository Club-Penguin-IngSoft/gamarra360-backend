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
import pe.com.gamarra360.backend.enums.EstadoPago;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeRequest;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeResponse;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.entity.Pago;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import pe.com.gamarra360.backend.pago.repository.PagoRepository;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
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

        if (orden.getEstado() == EstadoPago.PAGADO) {
            log.warn("Webhook duplicado para OrdenPago: {}. Ignorado.", ordenPagoId);
            return;
        }

        orden.confirmarPago();
        ordenPagoRepository.save(orden);

        pagoRepository.findByStripePaymentIntentId(intent.getId())
                .ifPresent(pago -> {
                    pago.confirmarPago();
                    pagoRepository.save(pago);
                });

        List<Pedido> pedidos = pedidoRepository.findByOrdenPagoId(orden.getId());

        for (Pedido pedido : pedidos) {
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

        log.info("OrdenPago {} marcada como PAGADA y transfers distribuidos.", ordenPagoId);
    }
}