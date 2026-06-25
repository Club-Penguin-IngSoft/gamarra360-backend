package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.enums.EstadoSolicitud;
import pe.com.gamarra360.backend.enums.TipoTrabajo;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionComercianteDetalle;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionComercianteResumen;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionDetalleResponse;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionRequest;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionResumen;
import pe.com.gamarra360.backend.solicitud.dto.ContraPropuestaRequest;
import pe.com.gamarra360.backend.solicitud.dto.RespuestaPersonalizacionRequest;
import pe.com.gamarra360.backend.solicitud.entity.ItemPersonalizado;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import pe.com.gamarra360.backend.solicitud.entity.RespuestaSolicitud;
import pe.com.gamarra360.backend.solicitud.repository.ItemPersonalizadoRepository;
import pe.com.gamarra360.backend.solicitud.repository.PersonalizacionRepository;
import pe.com.gamarra360.backend.solicitud.repository.RespuestaSolicitudRepository;
import pe.com.gamarra360.backend.solicitud.service.PersonalizacionService;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PersonalizacionServiceImpl extends AbstractCrudService<Personalizacion, Long> implements PersonalizacionService {

    private final PersonalizacionRepository personalizacionRepository;
    private final ComercianteRepository comercianteRepository;
    private final RespuestaSolicitudRepository respuestaSolicitudRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPersonalizadoRepository itemPersonalizadoRepository;
    private final ClienteRepository clienteRepository;

    public PersonalizacionServiceImpl(PersonalizacionRepository repository,
                                       ComercianteRepository comercianteRepository,
                                       RespuestaSolicitudRepository respuestaSolicitudRepository,
                                       DetallePedidoRepository detallePedidoRepository,
                                       PedidoRepository pedidoRepository,
                                       ItemPersonalizadoRepository itemPersonalizadoRepository,
                                       ClienteRepository clienteRepository) {
        super(repository, "Personalizacion");
        this.personalizacionRepository = repository;
        this.comercianteRepository = comercianteRepository;
        this.respuestaSolicitudRepository = respuestaSolicitudRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPersonalizadoRepository = itemPersonalizadoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Personalizacion entidad, Long id) {
        entidad.setId(id);
    }

    @Override
    @Transactional
    public Personalizacion crearSolicitud(PersonalizacionRequest request, Integer clienteId) {
        log.info("Creando solicitud de personalización para cliente {} y variante {}",
                clienteId, request.getDetalleProductoId());

        Personalizacion p = new Personalizacion();
        p.setClienteId(clienteId);
        p.setVendedorId(request.getVendedorId());
        p.setDetalleProductoId(request.getDetalleProductoId());

        if (request.getTipoPersonalizacion() != null) {
            p.setTipoPersonalizacion(TipoTrabajo.valueOf(request.getTipoPersonalizacion()));
        }

        p.setUrlLogo(request.getUrlLogo());
        p.setDescripcion(request.getDescripcion());
        p.setCantidad(request.getCantidad() != null ? request.getCantidad() : 1);

        Personalizacion saved = personalizacionRepository.save(p);
        log.info("Solicitud de personalización creada con ID {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalizacionResumen> listarPorCliente(Integer clienteId) {
        return personalizacionRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream()
                .map(this::toResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalizacionDetalleResponse obtenerDetalle(Long id, Integer clienteId) {
        Personalizacion p = obtenerYValidarPropietario(id, clienteId);
        return toDetalle(p);
    }

    @Override
    @Transactional
    public PersonalizacionDetalleResponse aceptar(Long id, Integer clienteId) {
        Personalizacion p = obtenerYValidarPropietario(id, clienteId);
        if (p.getEstado() != EstadoSolicitud.RESPONDIDA) {
            throw new ConflictoNegocioException("Solo se puede aceptar una personalización en estado RESPONDIDA.");
        }

        RespuestaSolicitud respuesta = respuestaSolicitudRepository.findByIdSolicitud(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("RespuestaSolicitud", id));

        ItemPersonalizado item = new ItemPersonalizado();
        item.setClienteId(p.getClienteId());
        item.setVendedorId(p.getVendedorId());
        item.setDetalleProductoId(p.getDetalleProductoId());
        item.setCantidad(p.getCantidad());
        item.setPrecioAcordado(respuesta.getPrecioPropuesto());
        item.setUrlLogo(p.getUrlLogo());
        item.setDescripcion(p.getDescripcion());
        item.setRespuestaId(respuesta.getIdRespuesta());
        itemPersonalizadoRepository.save(item);

        VarianteProducto variante = p.getVarianteProducto();
        if (variante != null && variante.getStock() != null && p.getCantidad() != null && p.getCantidad() > 0) {
            variante.setStock(Math.max(0, variante.getStock() - p.getCantidad()));
        }

        p.aceptar();
        personalizacionRepository.save(p);

        log.info("Personalización {} aceptada por cliente {} — ItemPersonalizado {} creado", id, clienteId, item.getId());
        return toDetalle(p);
    }

    @Override
    @Transactional
    public void rechazar(Long id, Integer clienteId) {
        Personalizacion p = obtenerYValidarPropietario(id, clienteId);
        if (p.getEstado() != EstadoSolicitud.RESPONDIDA) {
            throw new ConflictoNegocioException("Solo se puede rechazar una personalización en estado RESPONDIDA.");
        }
        p.cancelar();
        personalizacionRepository.save(p);
        log.info("Personalización {} rechazada por cliente {}", id, clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalizacionComercianteResumen> listarPorVendedor(Integer vendedorId) {
        return personalizacionRepository.findByVendedorIdOrderByFechaCreacionDesc(vendedorId).stream()
                .map(this::toComercianteResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalizacionComercianteDetalle obtenerDetalleComerciante(Long id, Integer vendedorId) {
        Personalizacion p = personalizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Personalizacion", id));
        if (!vendedorId.equals(p.getVendedorId())) {
            throw new AccessDeniedException("La personalización no pertenece al comerciante autenticado.");
        }
        return toComercianteDetalle(p);
    }

    @Override
    @Transactional
    public PersonalizacionComercianteDetalle responder(Long id, RespuestaPersonalizacionRequest request, Integer vendedorId) {
        Personalizacion p = personalizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Personalizacion", id));
        if (!vendedorId.equals(p.getVendedorId())) {
            throw new AccessDeniedException("La personalización no pertenece al comerciante autenticado.");
        }
        if (p.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ConflictoNegocioException("Solo se puede responder una personalización en estado PENDIENTE.");
        }

        RespuestaSolicitud respuesta = new RespuestaSolicitud();
        respuesta.setIdSolicitud(id);

        if ("RECHAZAR".equalsIgnoreCase(request.getDecision())) {
            respuesta.setComentario(request.getComentario());
            respuestaSolicitudRepository.save(respuesta);
            p.cancelar();
        } else if ("ACEPTAR".equalsIgnoreCase(request.getDecision())) {
            respuesta.setPrecioPropuesto(request.getPrecioPropuesto());
            respuesta.setAnotaciones(request.getAnotaciones());
            respuesta.setCondiciones(request.getCondiciones());
            respuestaSolicitudRepository.save(respuesta);
            p.marcarComoRespondida();
        } else {
            throw new ConflictoNegocioException("decision debe ser ACEPTAR o RECHAZAR.");
        }

        personalizacionRepository.save(p);
        log.info("Personalización {} respondida ({}) por vendedor {}", id, request.getDecision(), vendedorId);
        return toComercianteDetalle(p);
    }

    @Override
    @Transactional
    public void cancelarPorCliente(Long id, Integer clienteId) {
        Personalizacion p = obtenerYValidarPropietario(id, clienteId);
        if (p.getEstado() == EstadoSolicitud.ACEPTADA) {
            throw new ConflictoNegocioException("No se puede cancelar una personalización ya aceptada.");
        }
        if (p.getEstado() == EstadoSolicitud.RECHAZADA) {
            throw new ConflictoNegocioException("La personalización ya está cancelada.");
        }
        p.cancelar();
        personalizacionRepository.save(p);
        log.info("Personalización {} cancelada por cliente {}", id, clienteId);
    }

    @Override
    @Transactional
    public void cancelarPorVendedor(Long id, Integer vendedorId) {
        Personalizacion p = personalizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Personalizacion", id));
        if (!vendedorId.equals(p.getVendedorId())) {
            throw new AccessDeniedException("La personalización no pertenece al comerciante autenticado.");
        }
        if (p.getEstado() == EstadoSolicitud.ACEPTADA) {
            throw new ConflictoNegocioException("No se puede cancelar una personalización ya aceptada.");
        }
        if (p.getEstado() == EstadoSolicitud.RECHAZADA) {
            throw new ConflictoNegocioException("La personalización ya está cancelada.");
        }
        p.cancelar();
        personalizacionRepository.save(p);
        log.info("Personalización {} cancelada por vendedor {}", id, vendedorId);
    }

    @Override
    @Transactional
    public PersonalizacionDetalleResponse contraProponerCliente(Long id, ContraPropuestaRequest request, Integer clienteId) {
        Personalizacion p = obtenerYValidarPropietario(id, clienteId);
        if (p.getEstado() != EstadoSolicitud.RESPONDIDA) {
            throw new ConflictoNegocioException("Solo se puede enviar una contrapropuesta en estado RESPONDIDA.");
        }
        respuestaSolicitudRepository.findByIdSolicitud(id)
                .ifPresent(r -> respuestaSolicitudRepository.eliminarPorId(r.getIdRespuesta()));
        if (request.getEspecificacion() != null && !request.getEspecificacion().isBlank()) {
            p.setDescripcion(request.getEspecificacion());
        }
        if (request.getPrecioDeseado() != null) {
            p.setPrecioDeseado(request.getPrecioDeseado());
        }
        p.setEstado(EstadoSolicitud.PENDIENTE);
        Personalizacion saved = personalizacionRepository.save(p);
        log.info("Contrapropuesta enviada en personalización {} por cliente {}", id, clienteId);
        return toDetalle(saved);
    }

    private Personalizacion obtenerYValidarPropietario(Long id, Integer clienteId) {
        Personalizacion p = personalizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Personalizacion", id));
        if (!clienteId.equals(p.getClienteId())) {
            throw new AccessDeniedException("La personalización no pertenece al cliente autenticado.");
        }
        return p;
    }

    private PersonalizacionResumen toResumen(Personalizacion p) {
        VarianteProducto v = p.getVarianteProducto();
        Producto producto = v != null ? v.getProducto() : null;

        Comerciante comerciante = buscarComerciante(p.getVendedorId());
        String nombreTienda = comerciante != null && comerciante.getTienda() != null ? comerciante.getTienda().getNombreComercial() : null;
        String fotoTienda = comerciante != null && comerciante.getTienda() != null ? comerciante.getTienda().getFoto() : null;

        String pedidoEstado = buscarPedido(p.getId())
                .map(pedido -> pedido.getEstado() != null ? pedido.getEstado().name() : null)
                .orElse(null);

        return new PersonalizacionResumen(
                p.getId(),
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getFechaCreacion() != null ? p.getFechaCreacion().toString() : null,
                p.getVendedorId(),
                nombreTienda,
                fotoTienda,
                p.getDetalleProductoId(),
                producto != null ? producto.getNombre() : null,
                imagenPrincipal(producto),
                v != null && v.getTalla() != null ? v.getTalla().getTalla() : null,
                v != null && v.getColor() != null ? v.getColor().getNombre() : null,
                v != null ? v.getSku() : null,
                p.getCantidad(),
                calcularTotal(p, v, producto),
                pedidoEstado
        );
    }

    private PersonalizacionDetalleResponse toDetalle(Personalizacion p) {
        VarianteProducto v = p.getVarianteProducto();
        Producto producto = v != null ? v.getProducto() : null;

        Comerciante comerciante = buscarComerciante(p.getVendedorId());
        String nombreTienda = comerciante != null && comerciante.getTienda() != null ? comerciante.getTienda().getNombreComercial() : null;
        String fotoTienda = comerciante != null && comerciante.getTienda() != null ? comerciante.getTienda().getFoto() : null;

        int cantidad = p.getCantidad() != null ? p.getCantidad() : 1;
        Double precioUnitario = precioUnitario(v, producto);
        Double descuentoUnitario = calcularDescuentoUnitario(producto, precioUnitario, cantidad);
        Double precioBase = precioUnitario != null ? precioUnitario * cantidad : null;
        Double descuentos = precioBase != null ? descuentoUnitario * cantidad : null;

        RespuestaSolicitud respuesta = respuestaSolicitudRepository.findByIdSolicitud(p.getId()).orElse(null);
        Double costoPersonalizacion = respuesta != null ? respuesta.getPrecioPropuesto() : null;

        Double total = (precioBase != null ? precioBase - descuentos : 0.0)
                + (costoPersonalizacion != null ? costoPersonalizacion : 0.0);

        PersonalizacionDetalleResponse.PropuestaInfo propuestaInfo = respuesta != null
                ? new PersonalizacionDetalleResponse.PropuestaInfo(
                        respuesta.getIdRespuesta(),
                        respuesta.getPrecioPropuesto(),
                        respuesta.getComentario(),
                        respuesta.getCondiciones(),
                        respuesta.getAnotaciones(),
                        respuesta.getImagen(),
                        respuesta.getFecha() != null ? respuesta.getFecha().toString() : null)
                : null;

        PersonalizacionDetalleResponse.PedidoInfo pedidoInfo = buscarPedido(p.getId())
                .map(pedido -> new PersonalizacionDetalleResponse.PedidoInfo(
                        pedido.getId(),
                        pedido.getEstado() != null ? pedido.getEstado().name() : null,
                        pedido.getTipoEntrega() != null ? pedido.getTipoEntrega().name() : null,
                        pedido.getDireccionEntrega(),
                        pedido.getFechaActualizacion() != null ? pedido.getFechaActualizacion().toString() : null))
                .orElse(null);

        return new PersonalizacionDetalleResponse(
                p.getId(),
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getFechaCreacion() != null ? p.getFechaCreacion().toString() : null,
                p.getVendedorId(),
                nombreTienda,
                fotoTienda,
                p.getDetalleProductoId(),
                producto != null ? producto.getNombre() : null,
                imagenPrincipal(producto),
                v != null && v.getTalla() != null ? v.getTalla().getTalla() : null,
                v != null && v.getColor() != null ? v.getColor().getNombre() : null,
                v != null ? v.getSku() : null,
                p.getCantidad(),
                p.getUrlLogo(),
                p.getTipoPersonalizacion() != null ? p.getTipoPersonalizacion().name() : null,
                p.getDescripcion(),
                precioBase,
                descuentos,
                costoPersonalizacion,
                total,
                propuestaInfo,
                pedidoInfo,
                p.getPrecioDeseado()
        );
    }

    private PersonalizacionComercianteResumen toComercianteResumen(Personalizacion p) {
        Cliente cliente = p.getClienteId() != null ? clienteRepository.findById(p.getClienteId()).orElse(null) : null;
        String nombreCliente = cliente != null ? trimNombre(cliente.getNombres(), cliente.getPrimerApellido()) : null;
        String emailCliente = cliente != null ? cliente.getEmail() : null;

        String pedidoEstado = buscarPedido(p.getId())
                .map(pedido -> pedido.getEstado() != null ? pedido.getEstado().name() : null)
                .orElse(null);

        return new PersonalizacionComercianteResumen(
                p.getId(),
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getFechaCreacion() != null ? p.getFechaCreacion().toString() : null,
                p.getClienteId(),
                nombreCliente,
                emailCliente,
                pedidoEstado
        );
    }

    private PersonalizacionComercianteDetalle toComercianteDetalle(Personalizacion p) {
        VarianteProducto v = p.getVarianteProducto();
        Producto producto = v != null ? v.getProducto() : null;

        Cliente cliente = p.getClienteId() != null ? clienteRepository.findById(p.getClienteId()).orElse(null) : null;
        String nombreCliente = cliente != null ? trimNombre(cliente.getNombres(), cliente.getPrimerApellido()) : null;
        String emailCliente = cliente != null ? cliente.getEmail() : null;
        int totalPedidosCliente = pedidoRepository
                .findByClienteIdAndVendedorIdOrderByFechaDesc(p.getClienteId(), p.getVendedorId())
                .size();

        RespuestaSolicitud respuesta = respuestaSolicitudRepository.findByIdSolicitud(p.getId()).orElse(null);
        PersonalizacionComercianteDetalle.PropuestaInfo propuestaInfo = respuesta != null
                ? new PersonalizacionComercianteDetalle.PropuestaInfo(
                        respuesta.getPrecioPropuesto(),
                        respuesta.getComentario(),
                        respuesta.getCondiciones(),
                        respuesta.getAnotaciones(),
                        respuesta.getFecha() != null ? respuesta.getFecha().toString() : null)
                : null;

        return new PersonalizacionComercianteDetalle(
                p.getId(),
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getFechaCreacion() != null ? p.getFechaCreacion().toString() : null,
                p.getClienteId(),
                nombreCliente,
                emailCliente,
                totalPedidosCliente,
                producto != null ? producto.getNombre() : null,
                imagenPrincipal(producto),
                v != null && v.getTalla() != null ? v.getTalla().getTalla() : null,
                v != null && v.getColor() != null ? v.getColor().getNombre() : null,
                p.getCantidad(),
                p.getUrlLogo(),
                p.getTipoPersonalizacion() != null ? p.getTipoPersonalizacion().name() : null,
                p.getDescripcion(),
                propuestaInfo,
                p.getPrecioDeseado()
        );
    }

    private String trimNombre(String nombre, String apellido) {
        String n = nombre != null ? nombre.trim() : "";
        String a = apellido != null ? apellido.trim() : "";
        return (n + " " + a).trim();
    }

    private Comerciante buscarComerciante(Integer vendedorId) {
        return vendedorId != null ? comercianteRepository.findById(vendedorId).orElse(null) : null;
    }

    private Optional<Pedido> buscarPedido(Long personalizacionId) {
        return detallePedidoRepository.findByPersonalizacionId(personalizacionId)
                .flatMap(dp -> pedidoRepository.findById(dp.getPedidoId()));
    }

    private Double calcularTotal(Personalizacion p, VarianteProducto v, Producto producto) {
        int cantidad = p.getCantidad() != null ? p.getCantidad() : 1;
        Double precioUnitario = precioUnitario(v, producto);
        if (precioUnitario == null) {
            precioUnitario = 0.0;
        }
        Double descuentoUnitario = calcularDescuentoUnitario(producto, precioUnitario, cantidad);
        Double subtotal = (precioUnitario - descuentoUnitario) * cantidad;

        RespuestaSolicitud respuesta = respuestaSolicitudRepository.findByIdSolicitud(p.getId()).orElse(null);
        Double costoPersonalizacion = respuesta != null && respuesta.getPrecioPropuesto() != null
                ? respuesta.getPrecioPropuesto() : 0.0;

        return subtotal + costoPersonalizacion;
    }

    private Double precioUnitario(VarianteProducto v, Producto producto) {
        if (v != null && v.getPrecioAjustado() != null) {
            return v.getPrecioAjustado();
        }
        return producto != null ? producto.getPrecioBase() : null;
    }

    private Double calcularDescuentoUnitario(Producto producto, Double precioUnitario, Integer cantidad) {
        if (producto == null || precioUnitario == null || cantidad == null || producto.getDescuentosVolumen() == null) {
            return 0.0;
        }
        return producto.getDescuentosVolumen().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .filter(d -> d.getCantidadMinima() == null || cantidad >= d.getCantidadMinima())
                .filter(d -> d.getCantidadMaxima() == null || cantidad <= d.getCantidadMaxima())
                .map(d -> precioUnitario * (d.getPorcentajeDescuento() / 100.0))
                .max(Double::compareTo)
                .orElse(0.0);
    }

    private String imagenPrincipal(Producto producto) {
        if (producto == null || producto.getImagenes() == null) {
            return null;
        }
        return producto.getImagenes().stream()
                .filter(i -> Boolean.TRUE.equals(i.getEsPrincipal()))
                .findFirst()
                .or(() -> producto.getImagenes().stream().findFirst())
                .map(i -> i.getUrl())
                .orElse(null);
    }
}
