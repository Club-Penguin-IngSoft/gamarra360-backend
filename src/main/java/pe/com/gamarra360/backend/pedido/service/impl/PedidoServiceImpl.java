package pe.com.gamarra360.backend.pedido.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.enums.TipoEntrega;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.pedido.dto.DashboardResumenDTO;
import pe.com.gamarra360.backend.pedido.dto.DashboardResumenDTO.PedidoPorDia;
import pe.com.gamarra360.backend.pedido.dto.DashboardResumenDTO.ProductoTop;
import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteDetalle;
import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteResumen;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.pedido.service.PedidoService;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.dto.ResumenItemCotizacion;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import pe.com.gamarra360.backend.solicitud.repository.PersonalizacionRepository;
import pe.com.gamarra360.backend.solicitud.service.CotizacionService;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PedidoServiceImpl extends AbstractCrudService<Pedido, Long> implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ClienteRepository clienteRepository;
    private final PersonalizacionRepository personalizacionRepository;
    private final CotizacionService cotizacionService;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                              DetallePedidoRepository detallePedidoRepository,
                              ClienteRepository clienteRepository,
                              PersonalizacionRepository personalizacionRepository,
                              CotizacionService cotizacionService) {
        super(pedidoRepository, "Pedido");
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.clienteRepository = clienteRepository;
        this.personalizacionRepository = personalizacionRepository;
        this.cotizacionService = cotizacionService;
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

    /* ── Comerciante: avanzar estado del pedido ─────────────────────────── */

    private static final java.util.Map<EstadoPedido, EstadoPedido> SIGUIENTE_ESTADO_DELIVERY = java.util.Map.of(
            EstadoPedido.RECIBIDO,            EstadoPedido.EN_PREPARACION,
            EstadoPedido.EN_PREPARACION,      EstadoPedido.LISTO_PARA_ENTREGA,
            EstadoPedido.LISTO_PARA_ENTREGA,  EstadoPedido.EN_CAMINO,
            EstadoPedido.EN_CAMINO,           EstadoPedido.ENTREGADO
    );

    private static final java.util.Map<EstadoPedido, EstadoPedido> SIGUIENTE_ESTADO_RECOJO = java.util.Map.of(
            EstadoPedido.RECIBIDO,            EstadoPedido.EN_PREPARACION,
            EstadoPedido.EN_PREPARACION,      EstadoPedido.LISTO_PARA_ENTREGA,
            EstadoPedido.LISTO_PARA_ENTREGA,  EstadoPedido.ENTREGADO
    );

    @Override
    @Transactional
    public Pedido avanzarEstado(Long id, Integer vendedorId) {
        log.info("Avanzando estado del pedido {} — vendedorId={}", id, vendedorId);
        Pedido pedido = obtener(id);
        if (!vendedorId.equals(pedido.getVendedorId())) {
            throw new AccessDeniedException("El pedido no pertenece al comerciante autenticado.");
        }
        
        EstadoPedido siguiente;
        if (pedido.getTipoEntrega() == TipoEntrega.RECOJO_TIENDA) {
            siguiente = SIGUIENTE_ESTADO_RECOJO.get(pedido.getEstado());
        } else {
            siguiente = SIGUIENTE_ESTADO_DELIVERY.get(pedido.getEstado());
        }

        if (siguiente == null) {
            throw new ConflictoNegocioException("El pedido ya está en estado final: " + pedido.getEstado());
        }
        pedido.cambiarEstado(siguiente);
        actualizar(id, pedido);
        log.info("Pedido {} avanzó a {}", id, siguiente);
        return pedido;
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

    /* ── Dashboard ────────────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenDTO obtenerDashboard(Integer vendedorId, LocalDate desde, LocalDate hasta) {
        LocalDateTime desdeTs = desde.atStartOfDay();
        LocalDateTime hastaTs = hasta.plusDays(1).atStartOfDay();

        List<Pedido> pedidos = pedidoRepository
                .findByVendedorIdAndFechaBetweenOrderByFechaDesc(vendedorId, desdeTs, hastaTs);

        // 1. Pedidos por día
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Integer> porDiaMap = new LinkedHashMap<>();
        // Inicializar todos los días del rango a 0
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            porDiaMap.put(cursor.format(fmt), 0);
            cursor = cursor.plusDays(1);
        }
        for (Pedido p : pedidos) {
            if (p.getFecha() != null) {
                String dia = p.getFecha().toLocalDate().format(fmt);
                porDiaMap.merge(dia, 1, Integer::sum);
            }
        }
        List<PedidoPorDia> pedidosPorDia = porDiaMap.entrySet().stream()
                .map(e -> new PedidoPorDia(e.getKey(), e.getValue()))
                .toList();

        // 2. Pedidos recientes (últimos 5)
        List<PedidoComercianteResumen> pedidosRecientes = pedidos.stream()
                .limit(5)
                .map(this::toResumen)
                .toList();

        // 3. Pedidos completados (estado ENTREGADO)
        List<PedidoComercianteResumen> pedidosCompletados = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.RECIBIDO)
                .map(this::toResumen)
                .toList();

        // 4. Top 5 productos más solicitados y todos los productos ordenados
        List<Long> pedidoIds = pedidos.stream().map(Pedido::getId).toList();
        List<ProductoTop> topProductos = new ArrayList<>();
        List<ProductoTop> todosProductos = new ArrayList<>();
        if (!pedidoIds.isEmpty()) {
            List<DetallePedido> detalles = detallePedidoRepository
                    .findByVendedorPedidos(vendedorId, pedidoIds);

            // agrupar por producto
            Map<Integer, int[]> porProducto = new LinkedHashMap<>();
            Map<Integer, String> nombres = new LinkedHashMap<>();
            Map<Integer, String> imagenes = new LinkedHashMap<>();

            for (DetallePedido dp : detalles) {
                VarianteProducto v = dp.getVarianteProducto();
                if (v == null || v.getProducto() == null) continue;
                Producto prod = v.getProducto();
                Integer idProd = prod.getIdProducto();
                porProducto.computeIfAbsent(idProd, k -> new int[]{0})[0] += (dp.getCantidad() != null ? dp.getCantidad() : 0);
                nombres.putIfAbsent(idProd, prod.getNombre());
                imagenes.putIfAbsent(idProd, imagenPrincipal(prod));
            }

            // Ordenar todos los productos
            var productosOrdenados = porProducto.entrySet().stream()
                    .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                    .map(e -> new ProductoTop(
                            e.getKey(),
                            nombres.get(e.getKey()),
                            imagenes.get(e.getKey()),
                            e.getValue()[0]
                    ))
                    .collect(Collectors.toList());

            // Top 5 para la vista principal
            topProductos = productosOrdenados.stream()
                    .limit(5)
                    .collect(Collectors.toList());

            // Todos los productos para la pestaña
            todosProductos = productosOrdenados;
        }

        // 5. Total unidades y total ingresos
        int totalUnidades = pedidoIds.isEmpty() ? 0 :
                detallePedidoRepository.findByVendedorPedidos(vendedorId, pedidoIds)
                        .stream().mapToInt(dp -> dp.getCantidad() != null ? dp.getCantidad() : 0).sum();

        double totalIngresos = pedidos.stream()
                //.filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                .sum();

        return new DashboardResumenDTO(
                pedidosPorDia,
                topProductos,
                todosProductos,
                pedidosRecientes,
                pedidosCompletados,
                totalUnidades,
                totalIngresos
        );
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

        // Ítems provenientes de una cotización no tienen variante: tomamos nombre
        // e imagen del producto original de la cotización.
        if (nombreProducto == null && dp.getCotizacionId() != null) {
            ResumenItemCotizacion resumen = cotizacionService.obtenerResumenItem(dp.getCotizacionId());
            if (resumen != null) {
                nombreProducto = resumen.nombre();
                if (imagenUrl == null) imagenUrl = resumen.imagenUrl();
            }
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
