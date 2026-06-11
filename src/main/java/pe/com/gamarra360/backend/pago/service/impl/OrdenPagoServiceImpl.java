package pe.com.gamarra360.backend.pago.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.entity.OrdenPagoDetalleResponse;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import pe.com.gamarra360.backend.pago.service.OrdenPagoService;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class OrdenPagoServiceImpl extends AbstractCrudService<OrdenPago, Long> implements OrdenPagoService {

    private final OrdenPagoRepository ordenPagoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ComercianteRepository comercianteRepository;

    public OrdenPagoServiceImpl(OrdenPagoRepository ordenPagoRepository,
                                 PedidoRepository pedidoRepository,
                                 DetallePedidoRepository detallePedidoRepository,
                                 ComercianteRepository comercianteRepository) {
        super(ordenPagoRepository, "OrdenPago");
        this.ordenPagoRepository = ordenPagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.comercianteRepository = comercianteRepository;
    }

    @Override
    @Transactional
    public void marcarComoPagado(Long ordenId) {
        OrdenPago orden = ordenPagoRepository.findById(ordenId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden no encontrada: " + ordenId));
        orden.confirmarPago(); // usa el mismo método que el webhook
        ordenPagoRepository.save(orden);
        log.info("OrdenPago {} marcada como PAGADA desde frontend.", ordenId);
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(OrdenPago entidad, Long id) { entidad.setId(id); }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenPago> listarPorCliente(Integer clienteId) {
        return ordenPagoRepository.findByClienteIdOrderByFechaDesc(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenPagoDetalleResponse obtenerDetalle(Long ordenPagoId) {
        OrdenPago orden = ordenPagoRepository.findById(ordenPagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de pago no encontrada: " + ordenPagoId));

        List<OrdenPagoDetalleResponse.PedidoResumen> pedidosResumen =
                pedidoRepository.findByOrdenPagoId(ordenPagoId).stream()
                        .map(this::toPedidoResumen)
                        .toList();

        return new OrdenPagoDetalleResponse(
                orden.getId(),
                orden.getClienteId(),
                orden.getTotal(),
                orden.getEstado() != null ? orden.getEstado().name() : null,
                orden.getFecha() != null ? orden.getFecha().toString() : null,
                pedidosResumen
        );
    }

    private OrdenPagoDetalleResponse.PedidoResumen toPedidoResumen(Pedido p) {
        String nombreTienda = null;
        String fotoTienda = null;
        if (p.getVendedorId() != null) {
            var comercianteOpt = comercianteRepository.findById(p.getVendedorId());
            if (comercianteOpt.isPresent()) {
                var comerciante = comercianteOpt.get();
                nombreTienda = comerciante.getNombreTienda();
                if (comerciante.getTienda() != null) {
                    fotoTienda = comerciante.getTienda().getFoto();
                }
            }
        }

        List<OrdenPagoDetalleResponse.DetalleResumen> detalles =
                detallePedidoRepository.findByPedidoId(p.getId()).stream()
                        .map(this::toDetalleResumen)
                        .toList();

        return new OrdenPagoDetalleResponse.PedidoResumen(
                p.getId(),
                p.getVendedorId(),
                nombreTienda,
                fotoTienda,
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getTipoEntrega() != null ? p.getTipoEntrega().name() : null,
                p.getDireccionEntrega(),
                p.getTotal(),
                p.getFecha() != null ? p.getFecha().toString() : null,
                p.getFechaActualizacion() != null ? p.getFechaActualizacion().toString() : null,
                detalles
        );
    }

    private OrdenPagoDetalleResponse.DetalleResumen toDetalleResumen(DetallePedido dp) {
        VarianteProducto v = dp.getVarianteProducto();
        String imagenUrl = null;
        if (v != null && v.getProducto() != null) {
            imagenUrl = v.getProducto().getImagenes().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getEsPrincipal()))
                    .findFirst()
                    .or(() -> v.getProducto().getImagenes().stream().findFirst())
                    .map(i -> i.getUrl())
                    .orElse(null);
        }
        return new OrdenPagoDetalleResponse.DetalleResumen(
                dp.getId(),
                dp.getIdVarianteProducto(),
                v != null && v.getProducto() != null ? v.getProducto().getIdProducto() : null,
                v != null && v.getProducto() != null ? v.getProducto().getNombre() : null,
                imagenUrl,
                v != null && v.getTalla() != null ? v.getTalla().getTalla() : null,
                v != null && v.getColor() != null ? v.getColor().getNombre() : null,
                v != null ? v.getSku() : null,
                dp.getCantidad(),
                dp.getPrecio()
        );
    }
}
