package pe.com.gamarra360.backend.pedido.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.pedido.entity.PedidoRequestDTO;
import pe.com.gamarra360.backend.pedido.entity.PedidoResponseDTO;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.enums.TipoEntrega;import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;

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

    @Transactional
    public PedidoResponseDTO procesarCompra(PedidoRequestDTO dto, Integer clienteId) {

        log.info("Procesando compra simulada — clienteId={}, items={}, total={}",
                clienteId, dto.items().size(), dto.total());

        // ── 1. Crear OrdenPago ────────────────────────────────────────────
        OrdenPago ordenPago = new OrdenPago();
        ordenPago.setClienteId(clienteId);
        ordenPago.setTotal(dto.total());
        // @PrePersist asigna estado=PENDIENTE y fecha=now()
        ordenPago = ordenPagoRepository.save(ordenPago);

        // ── 2. Simular pago exitoso ───────────────────────────────────────
        ordenPago.confirmarPago();   // estado → PAGADO
        ordenPago = ordenPagoRepository.save(ordenPago);

        log.info("OrdenPago #{} confirmada (PAGADO)", ordenPago.getId());

        // ── 3. Crear Pedido ───────────────────────────────────────────────
        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setVendedorId(dto.vendedorId());
        pedido.setOrdenPagoId(ordenPago.getId());
        pedido.setTotal(dto.total());
        pedido.setTipoEntrega(TipoEntrega.valueOf(dto.tipoEntrega()));
        pedido.setDireccionEntrega(dto.direccionEntrega());
        // @PrePersist asigna estado=PENDIENTE_CONFIRMACION y fecha=now()
        pedido = pedidoRepository.save(pedido);

        log.info("Pedido #{} creado", pedido.getId());

        // ── 4. Crear DetallePedido por cada ítem ──────────────────────────
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

        // ── 5. Retornar respuesta ─────────────────────────────────────────
        return new PedidoResponseDTO(
                pedido.getId(),
                ordenPago.getId(),
                pedido.getEstado().name(),
                ordenPago.getEstado().name(),
                pedido.getTotal()
        );
    }
}
