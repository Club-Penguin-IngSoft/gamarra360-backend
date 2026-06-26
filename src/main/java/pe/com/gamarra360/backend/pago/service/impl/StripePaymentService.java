package pe.com.gamarra360.backend.pago.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pe.com.gamarra360.backend.catalogo.entity.Oferta;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.enums.EstadoPago;
import pe.com.gamarra360.backend.logistica.repository.DistritoEnvioRepository;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.enums.TipoEntrega;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeRequest;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeResponse;
import pe.com.gamarra360.backend.pago.dto.GrupoTiendaDto;
import pe.com.gamarra360.backend.pago.dto.PrepararCarritoRequest;
import pe.com.gamarra360.backend.pago.dto.PrepararCarritoResponse;
import pe.com.gamarra360.backend.pago.entity.CarritoPendiente;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.entity.Pago;
import pe.com.gamarra360.backend.pago.repository.CarritoPendienteRepository;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import pe.com.gamarra360.backend.pago.repository.PagoRepository;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionRepository;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripePaymentService {

    private final OrdenPagoRepository        ordenPagoRepository;
    private final PedidoRepository           pedidoRepository;
    private final ComercianteRepository      comercianteRepository;
    private final PagoRepository             pagoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final DetallePedidoRepository    detallePedidoRepository;
    private final CarritoPendienteRepository carritoPendienteRepository;
    private final CotizacionRepository       cotizacionRepository;
    private final DistritoEnvioRepository    distritoEnvioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${stripe.commission-rate:0.10}")
    private double commissionRate;

    @Transactional
    public PrepararCarritoResponse prepararCarrito(PrepararCarritoRequest request) {
        try {
            // Calcular costo de entrega real desde BD por cada grupo
            double costoEntregaTotal = 0.0;
            double subtotalItems = 0.0;

            for (GrupoTiendaDto grupo : request.getGrupos()) {
                for (GrupoTiendaDto.ItemCarritoDto item : grupo.getItems()) {
                    int cantidad = item.getCantidad() != null ? item.getCantidad() : 0;
                    if (item.getIdVarianteProducto() != null) {
                        // Variante normal: precio calculado server-side desde BD
                        subtotalItems += resolverPrecioVariante(item.getIdVarianteProducto()) * cantidad;
                    } else {
                        // Cotización / personalización: confiamos en el precio enviado por el frontend
                        subtotalItems += (item.getPrecio() != null ? item.getPrecio() : 0.0) * cantidad;
                    }
                }

                if ("DELIVERY".equals(grupo.getTipoEntrega()) && grupo.getIdDistrito() != null) {
                    var distrito = distritoEnvioRepository.findById(grupo.getIdDistrito()).orElse(null);
                    if (distrito != null && distrito.getCostoEnvio() != null) {
                        costoEntregaTotal += distrito.getCostoEnvio();
                    }
                }
                // RECOJO_TIENDA → costoEnvio = 0 (no suma)
            }

            double totalReal = subtotalItems + costoEntregaTotal;
            String json = objectMapper.writeValueAsString(request.getGrupos());

            CarritoPendiente carrito = new CarritoPendiente();
            carrito.setClienteId(request.getClienteId());
            carrito.setDatosJson(json);
            carrito.setTotal(totalReal);

            carrito = carritoPendienteRepository.save(carrito);
            log.info("CarritoPendiente {} — cliente={}, items=S/{}, entrega=S/{}, total=S/{}",
                    carrito.getId(), request.getClienteId(), subtotalItems, costoEntregaTotal, totalReal);

            return new PrepararCarritoResponse(carrito.getId(), subtotalItems, costoEntregaTotal, totalReal);
        } catch (Exception e) {
            log.error("Error preparando carrito: {}", e.getMessage(), e);
            throw new IllegalStateException("No se pudo preparar el carrito.", e);
        }
    }

    @Transactional
    public CrearPagoStripeResponse crearPaymentIntent(CrearPagoStripeRequest request)
            throws StripeException {

        CarritoPendiente carrito = carritoPendienteRepository.findById(request.carritoPendienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Carrito pendiente no encontrado: " + request.carritoPendienteId()));

        double total = carrito.getTotal();
        if (total <= 0) {
            throw new IllegalArgumentException("El carrito no tiene un monto válido.");
        }

        long totalCentimos = Math.round(total * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalCentimos)
                .setCurrency("usd")
                .setDescription("Compra Gamarra360 - Carrito #" + carrito.getId())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .putMetadata("carrito_pendiente_id", String.valueOf(carrito.getId()))
                .putMetadata("cliente_id", String.valueOf(carrito.getClienteId()))
                .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("carrito-" + carrito.getId())
                .build();

        PaymentIntent intent = PaymentIntent.create(params, options);
        log.info("PaymentIntent creado/recuperado: {} para CarritoPendiente: {} — S/ {}",
                intent.getId(), carrito.getId(), total);

        // Si ya existe un Pago para este PaymentIntent (reintento/recarga), lo reutilizamos.
        Pago pago = pagoRepository.findByStripePaymentIntentId(intent.getId())
                .orElseGet(() -> {
                    Pago nuevo = new Pago();
                    nuevo.setMonto(total);
                    nuevo.setMetodo("STRIPE");
                    nuevo.setStripePaymentIntentId(intent.getId());
                    nuevo.setStripeClientSecret(intent.getClientSecret());
                    nuevo.setEstado(EstadoPago.PENDIENTE);
                    return pagoRepository.save(nuevo);
                });

        return new CrearPagoStripeResponse(
                pago.getId(),
                intent.getClientSecret(),
                totalCentimos,
                "usd"
        );
    }

    @Transactional
    public void procesarPagoConfirmado(PaymentIntent intent) throws StripeException {
        String carritoPendienteIdStr = intent.getMetadata().get("carrito_pendiente_id");
        if (carritoPendienteIdStr == null) {
            log.warn("PaymentIntent {} sin carrito_pendiente_id en metadata — ignorado.", intent.getId());
            return;
        }

        Long carritoPendienteId = Long.valueOf(carritoPendienteIdStr);

        CarritoPendiente carrito = carritoPendienteRepository.findById(carritoPendienteId)
                .orElse(null);

        if (carrito == null) {
            log.warn("CarritoPendiente {} ya fue procesado o no existe — ignorado (idempotencia).",
                    carritoPendienteId);
            return;
        }

        OrdenPago orden = new OrdenPago();
        orden.setClienteId(carrito.getClienteId());
        orden.setTotal(carrito.getTotal());
        orden.setEstado(EstadoPago.PENDIENTE);
        orden.setFecha(LocalDateTime.now());
        orden = ordenPagoRepository.save(orden);
        orden.confirmarPago();
        ordenPagoRepository.save(orden);
        final Long ordenIdFinal = orden.getId();

        pagoRepository.findByStripePaymentIntentId(intent.getId()).ifPresent(pago -> {
            pago.setOrdenPagoId(ordenIdFinal);
            pago.confirmarPago();
            pagoRepository.save(pago);
        });

        List<GrupoTiendaDto> grupos;
        try {
            grupos = objectMapper.readValue(
                    carrito.getDatosJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GrupoTiendaDto.class));
        } catch (Exception e) {
            log.error("Error deserializando CarritoPendiente {}: {}", carritoPendienteId, e.getMessage(), e);
            throw new IllegalStateException("Carrito pendiente corrupto.", e);
        }

        for (GrupoTiendaDto grupo : grupos) {
            Pedido pedido = new Pedido();
            pedido.setClienteId(carrito.getClienteId());
            pedido.setVendedorId(grupo.getVendedorId());
            pedido.setOrdenPagoId(orden.getId());
            pedido.setTipoEntrega(TipoEntrega.valueOf(grupo.getTipoEntrega()));
            pedido.setDireccionEntrega(grupo.getDireccionEntrega());
            pedido.setTotal(grupo.getTotal());
            pedido.setEstado(EstadoPedido.RECIBIDO);
            pedido.setFecha(LocalDateTime.now());
            pedido.setFechaActualizacion(LocalDateTime.now());
            pedido = pedidoRepository.save(pedido);

            log.info("Pedido {} creado para vendedor {} (orden {})",
                    pedido.getId(), grupo.getVendedorId(), orden.getId());

            final Long pedidoIdFinal = pedido.getId();
            for (GrupoTiendaDto.ItemCarritoDto item : grupo.getItems()) {
                DetallePedido detalle = new DetallePedido();
                detalle.setPedidoId(pedidoIdFinal);
                detalle.setIdVarianteProducto(item.getIdVarianteProducto());
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecio(item.getPrecio());
                if (item.getPersonalizacionId() != null) {
                    detalle.setPersonalizacionId(item.getPersonalizacionId());
                }
                if (item.getCotizacionId() != null) {
                    detalle.setCotizacionId(item.getCotizacionId());
                    cotizacionRepository.findById(item.getCotizacionId()).ifPresent(cotizacion -> {
                        cotizacion.setPedidoId(pedidoIdFinal);
                        cotizacionRepository.save(cotizacion);
                    });
                }
                detallePedidoRepository.save(detalle);
            }

            procesarStockYTransfer(intent, pedido, grupo);
        }

        carritoPendienteRepository.delete(carrito);
        log.info("OrdenPago {} creada y confirmada a partir de CarritoPendiente {} (eliminado).",
                orden.getId(), carritoPendienteId);
    }

    private void procesarStockYTransfer(PaymentIntent intent, Pedido pedido, GrupoTiendaDto grupo) {
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

        if (pedido.getVendedorId() == null) return;
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
                        .setCurrency("usd")
                        .setDestination(comerciante.getStripeAccountId())
                        .setSourceTransaction(intent.getLatestCharge())
                        .putMetadata("pedido_id",   String.valueOf(pedido.getId()))
                        .putMetadata("vendedor_id", String.valueOf(pedido.getVendedorId()))
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

        try {
            List<DetallePedido> todosLosDetalles =
                    detallePedidoRepository.findByPedidoId(pedido.getId());
            detallePedidoRepository.deleteAll(todosLosDetalles);
            log.info("Detalles del pedido {} eliminados", pedido.getId());

            pedidoRepository.deleteById(pedido.getId());
            log.info("Pedido {} eliminado por stock insuficiente", pedido.getId());

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

    private Double resolverPrecioVariante(Integer idVariante) {
        return varianteProductoRepository.findById(idVariante)
                .map(v -> {
                    var producto = v.getProducto();
                    Double base = v.getPrecioAjustado() != null
                            ? v.getPrecioAjustado()
                            : (producto != null ? producto.getPrecioBase() : null);
                    if (base == null) return 0.0;
                    Oferta oferta = producto != null ? producto.getOferta() : null;
                    return esOfertaActiva(oferta) ? aplicarOferta(base, oferta) : base;
                })
                .orElse(0.0);
    }

    private boolean esOfertaActiva(Oferta oferta) {
        if (oferta == null || !Boolean.TRUE.equals(oferta.getActiva())) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(oferta.getFechaInicio()) && !now.isAfter(oferta.getFechaFin());
    }

    private double aplicarOferta(double base, Oferta oferta) {
        return switch (oferta.getTipoDescuento()) {
            case PORCENTAJE -> base * (1 - oferta.getValorDescuento() / 100.0);
            case MONTO_FIJO -> Math.max(0.0, base - oferta.getValorDescuento());
        };
    }
}