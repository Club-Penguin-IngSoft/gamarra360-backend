package pe.com.gamarra360.backend.pedido.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.enums.TipoEntrega;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.logistica.entity.DistritoEnvio;
import pe.com.gamarra360.backend.logistica.repository.DistritoEnvioRepository;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.entity.PedidoRequestDTO;
import pe.com.gamarra360.backend.pedido.entity.PedidoResponseDTO;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;

import java.time.LocalDate;

/**
 * Orquesta la creación de OrdenPago → Pedido → DetallePedido en una sola
 * transacción. Si cualquier paso falla, todo hace rollback.
 *
 * Flujo:
 *  1. Crear OrdenPago con estado PENDIENTE
 *  2. Confirmar pago (simula pasarela) → estado PAGADO
 *  3. Crear Pedido vinculado a la OrdenPago
 *  4. Crear DetallePedido por cada ítem
 *  5. Retornar PedidoResponseDTO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrdenPagoRepository ordenPagoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final DistritoEnvioRepository distritoEnvioRepository;

    @Transactional
    public PedidoResponseDTO procesarCompra(PedidoRequestDTO dto, Integer clienteId) {

        // ── 1. Calcular subtotal desde los ítems (no confiar en dto.total()) ──
        double subtotal = dto.items().stream()
                .mapToDouble(i -> i.precio() * i.cantidad())
                .sum();

        // ── 2. Resolver logística según tipo de entrega ───────────────────
        TipoEntrega tipo = TipoEntrega.valueOf(dto.tipoEntrega());
        double costoEnvio = 0.0;
        LocalDate fechaEntregaEstimada;
        DistritoEnvio distrito = null;

        if (tipo == TipoEntrega.DELIVERY) {
            if (dto.idDistrito() == null) {
                throw new DatosInvalidosException("El distrito es obligatorio para delivery.");
            }
            distrito = distritoEnvioRepository.findById(dto.idDistrito())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Distrito no encontrado con id " + dto.idDistrito()));
            costoEnvio = distrito.getCostoEnvio() != null ? distrito.getCostoEnvio() : 0.0;
            fechaEntregaEstimada = LocalDate.now().plusDays(2);
        } else {
            // RECOJO_TIENDA
            costoEnvio = 0.0;
            fechaEntregaEstimada = LocalDate.now().plusDays(1);
        }

        double totalFinal = subtotal + costoEnvio;

        log.info("Checkout — clienteId={}, tipo={}, subtotal={}, costoEnvio={}, total={}",
                clienteId, tipo, subtotal, costoEnvio, totalFinal);

        // ── 3. Crear OrdenPago ────────────────────────────────────────────
        OrdenPago ordenPago = new OrdenPago();
        ordenPago.setClienteId(clienteId);
        ordenPago.setTotal(totalFinal);
        ordenPago = ordenPagoRepository.save(ordenPago);

        // ── 4. Simular pago exitoso ───────────────────────────────────────
        ordenPago.confirmarPago();
        ordenPago = ordenPagoRepository.save(ordenPago);
        log.info("OrdenPago #{} confirmada (PAGADO)", ordenPago.getId());

        // ── 5. Crear Pedido ───────────────────────────────────────────────
        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setVendedorId(dto.vendedorId());
        pedido.setOrdenPagoId(ordenPago.getId());
        pedido.setTotal(totalFinal);
        pedido.setCostoEnvio(costoEnvio);
        pedido.setFechaEntregaEstimada(fechaEntregaEstimada);
        pedido.setTipoEntrega(tipo);
        pedido.setDireccionEntrega(dto.direccionEntrega());
        if (distrito != null) {
            pedido.setIdDistrito(distrito.getIdDistrito());
        }
        pedido = pedidoRepository.save(pedido);
        log.info("Pedido #{} creado", pedido.getId());

        // ── 6. Crear DetallePedido por cada ítem ──────────────────────────
        final Long pedidoId = pedido.getId();
        dto.items().forEach(item -> {
            DetallePedido detalle = new DetallePedido();
            detalle.setPedidoId(pedidoId);
            detalle.setIdVarianteProducto(item.idVarianteProducto());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecio(item.precio());
            detallePedidoRepository.save(detalle);
        });
        log.info("Pedido #{} — {} detalles guardados", pedidoId, dto.items().size());

        // ── 7. Retornar respuesta ─────────────────────────────────────────
        return new PedidoResponseDTO(
                pedido.getId(),
                ordenPago.getId(),
                pedido.getEstado().name(),
                ordenPago.getEstado().name(),
                totalFinal,
                costoEnvio,
                distrito != null ? distrito.getNombre() : null,
                distrito != null ? distrito.getCiudad() : null,
                fechaEntregaEstimada.toString()
        );
    }
}
