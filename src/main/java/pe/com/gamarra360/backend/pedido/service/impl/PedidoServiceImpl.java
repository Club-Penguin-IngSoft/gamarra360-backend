package pe.com.gamarra360.backend.pedido.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteDetalle;
import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteResumen;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.pedido.service.PedidoService;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import pe.com.gamarra360.backend.solicitud.repository.PersonalizacionRepository;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class PedidoServiceImpl extends AbstractCrudService<Pedido, Long> implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ClienteRepository clienteRepository;
    private final PersonalizacionRepository personalizacionRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                              DetallePedidoRepository detallePedidoRepository,
                              ClienteRepository clienteRepository,
                              PersonalizacionRepository personalizacionRepository) {
        super(pedidoRepository, "Pedido");
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.clienteRepository = clienteRepository;
        this.personalizacionRepository = personalizacionRepository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Pedido entidad, Long id) {
        entidad.setId(id);
    }

    @Override
    public void cancelar(Long id, Integer clienteId) {
        log.info("Cancelando pedido {} — clienteId={}", id, clienteId);
        Pedido pedido = obtener(id);
        if (!pedido.getClienteId().equals(clienteId)) {
            throw new AccessDeniedException("No tienes permiso para cancelar este pedido.");
        }
        pedido.cambiarEstado(EstadoPedido.CANCELADO);
        actualizar(id, pedido);
    }

    /* ── Comerciante: listar pedidos ─────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public List<PedidoComercianteResumen> listarPorVendedor(Integer vendedorId) {
        return pedidoRepository.findByVendedorIdOrderByFechaDesc(vendedorId)
                .stream().map(this::toResumen).toList();
    }

    /* ── Comerciante: detalle de pedido ──────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public PedidoComercianteDetalle obtenerDetalleComerciante(Long id, Integer vendedorId) {
        Pedido pedido = obtener(id);
        if (!vendedorId.equals(pedido.getVendedorId())) {
            throw new AccessDeniedException("El pedido no pertenece al comerciante autenticado.");
        }
        return toDetalle(pedido, vendedorId);
    }

    /* ── Helpers privados ────────────────────────────────────────────────── */

    private PedidoComercianteResumen toResumen(Pedido p) {
        Cliente cliente = p.getClienteId() != null
                ? clienteRepository.findById(p.getClienteId()).orElse(null) : null;

        return new PedidoComercianteResumen(
                p.getId(),
                p.getFecha() != null ? p.getFecha().toString() : null,
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getTotal(),
                p.getClienteId(),
                cliente != null ? trimNombre(cliente.getNombres(), cliente.getPrimerApellido()) : null,
                cliente != null ? cliente.getEmail() : null
        );
    }

    private PedidoComercianteDetalle toDetalle(Pedido p, Integer vendedorId) {
        Cliente cliente = p.getClienteId() != null
                ? clienteRepository.findById(p.getClienteId()).orElse(null) : null;

        List<PedidoComercianteDetalle.ItemDetalle> items = detallePedidoRepository.findByPedidoId(p.getId())
                .stream().map(this::toItemDetalle).toList();

        List<PedidoComercianteDetalle.HistorialPedido> historial =
                pedidoRepository.findByClienteIdAndVendedorIdOrderByFechaDesc(p.getClienteId(), vendedorId)
                        .stream()
                        .filter(h -> !h.getId().equals(p.getId()))
                        .map(h -> new PedidoComercianteDetalle.HistorialPedido(
                                h.getId(),
                                h.getFecha() != null ? h.getFecha().toString() : null,
                                h.getEstado() != null ? h.getEstado().name() : null,
                                h.getTotal()
                        ))
                        .toList();

        return new PedidoComercianteDetalle(
                p.getId(),
                p.getFecha() != null ? p.getFecha().toString() : null,
                p.getFechaActualizacion() != null ? p.getFechaActualizacion().toString() : null,
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getTotal(),
                p.getTipoEntrega() != null ? p.getTipoEntrega().name() : null,
                p.getDireccionEntrega(),
                p.getClienteId(),
                cliente != null ? trimNombre(cliente.getNombres(), cliente.getPrimerApellido()) : null,
                cliente != null ? cliente.getEmail() : null,
                items,
                historial
        );
    }

    private PedidoComercianteDetalle.ItemDetalle toItemDetalle(DetallePedido dp) {
        VarianteProducto v = dp.getVarianteProducto();
        String nombreProducto = null;
        String imagenUrl = null;
        String talla = null;
        String color = null;
        String sku = null;
        if (v != null) {
            if (v.getProducto() != null) {
                nombreProducto = v.getProducto().getNombre();
                imagenUrl = imagenPrincipal(v.getProducto());
            }
            talla = v.getTalla() != null ? v.getTalla().getTalla() : null;
            color = v.getColor() != null ? v.getColor().getNombre() : null;
            sku = v.getSku();
        }

        PedidoComercianteDetalle.PersonalizacionInfo personalizacion = null;
        if (dp.getPersonalizacionId() != null) {
            personalizacion = personalizacionRepository.findById(dp.getPersonalizacionId())
                    .map(this::toPersonalizacionInfo)
                    .orElse(null);
        }

        return new PedidoComercianteDetalle.ItemDetalle(
                dp.getId(),
                dp.getIdVarianteProducto(),
                nombreProducto,
                imagenUrl,
                talla,
                color,
                sku,
                dp.getCantidad(),
                dp.getPrecio(),
                personalizacion
        );
    }

    private PedidoComercianteDetalle.PersonalizacionInfo toPersonalizacionInfo(Personalizacion personalizacion) {
        return new PedidoComercianteDetalle.PersonalizacionInfo(
                personalizacion.getId(),
                personalizacion.getTipoPersonalizacion() != null ? personalizacion.getTipoPersonalizacion().name() : null,
                personalizacion.getDescripcion(),
                personalizacion.getUrlLogo(),
                personalizacion.getCantidad()
        );
    }

    private String imagenPrincipal(Producto producto) {
        if (producto == null || producto.getImagenes() == null) return null;
        return producto.getImagenes().stream()
                .filter(i -> Boolean.TRUE.equals(i.getEsPrincipal()))
                .findFirst()
                .or(() -> producto.getImagenes().stream().findFirst())
                .map(i -> i.getUrl())
                .orElse(null);
    }

    private String trimNombre(String nombre, String apellido) {
        String n = nombre != null ? nombre.trim() : "";
        String a = apellido != null ? apellido.trim() : "";
        return (n + " " + a).trim();
    }
}
