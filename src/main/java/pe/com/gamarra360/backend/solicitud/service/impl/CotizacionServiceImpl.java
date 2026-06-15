package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;

import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.enums.EstadoSolicitud;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.dto.*;
import pe.com.gamarra360.backend.solicitud.entity.*;
import pe.com.gamarra360.backend.solicitud.repository.*;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CotizacionServiceImpl extends AbstractCrudService<Cotizacion, Long> implements
        pe.com.gamarra360.backend.solicitud.service.CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final TiendaRepository tiendaRepository;
    private final DetalleCotizacionRepository detalleCotizacionRepository;
    private final CotizacionCatalogoRepository cotizacionCatalogoRepository;
    private final ProductoCotizadoManualRepository productoCotizadoManualRepository;
    private final RespuestaSolicitudRepository respuestaSolicitudRepository;
    private final ClienteRepository clienteRepository;

    public CotizacionServiceImpl(CotizacionRepository cotizacionRepository,
                                  TiendaRepository tiendaRepository,
                                  DetalleCotizacionRepository detalleCotizacionRepository,
                                  CotizacionCatalogoRepository cotizacionCatalogoRepository,
                                  ProductoCotizadoManualRepository productoCotizadoManualRepository,
                                  RespuestaSolicitudRepository respuestaSolicitudRepository,
                                  ClienteRepository clienteRepository) {
        super(cotizacionRepository, "Cotizacion");
        this.cotizacionRepository = cotizacionRepository;
        this.tiendaRepository = tiendaRepository;
        this.detalleCotizacionRepository = detalleCotizacionRepository;
        this.cotizacionCatalogoRepository = cotizacionCatalogoRepository;
        this.productoCotizadoManualRepository = productoCotizadoManualRepository;
        this.respuestaSolicitudRepository = respuestaSolicitudRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(Cotizacion entidad, Long id) { entidad.setId(id); }

    /* ── Crear solicitud de cotización ──────────────────────────────────── */

    @Override
    @Transactional
    public CotizacionDetalleResponse crearSolicitud(CotizacionRequest request, Integer clienteId) {
        log.info("Creando cotización para cliente {} en tienda {}", clienteId, request.getIdTienda());

        Tienda tienda = tiendaRepository.findById(request.getIdTienda())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda con id " + request.getIdTienda()));

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setClienteId(clienteId);
        cotizacion.setVendedorId(tienda.getIdComerciante());
        cotizacion.setIdTienda(tienda.getIdTienda());
        Cotizacion saved = cotizacionRepository.save(cotizacion);

        for (ProductoCotizacionDto dto : request.getProductos()) {
            if ("CATALOGO".equalsIgnoreCase(dto.getTipo()) && dto.getIdVariante() != null) {
                CotizacionCatalogo cc = new CotizacionCatalogo();
                cc.setIdCotizacion(saved.getId());
                cc.setIdDetalleProducto(dto.getIdVariante());
                cc.setEspecificacion(dto.getEspecificacion());
                cc.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : 1);
                cotizacionCatalogoRepository.save(cc);
            } else {
                DetalleCotizacion dc = new DetalleCotizacion();
                dc.setIdCotizacion(saved.getId());
                dc.setEspecificacion(dto.getEspecificacion());
                dc.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : 1);
                DetalleCotizacion savedDc = detalleCotizacionRepository.save(dc);

                ProductoCotizadoManual manual = new ProductoCotizadoManual();
                manual.setDetalleCotizacionId(savedDc.getDetalleCotizacionId());
                manual.setNombre(dto.getNombre() != null ? dto.getNombre() : "Producto");
                manual.setImagen(dto.getImagenUrl());
                manual.setEspecificacion(dto.getEspecificacion());
                productoCotizadoManualRepository.save(manual);
            }
        }

        log.info("Cotización {} creada con {} producto(s)", saved.getId(), request.getProductos().size());
        return toDetalle(saved);
    }

    /* ── Listar por cliente ──────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionResumen> listarPorCliente(Integer clienteId) {
        return cotizacionRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId)
                .stream().map(this::toResumen).toList();
    }

    /* ── Listar por vendedor ─────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionResumen> listarPorVendedor(Integer vendedorId) {
        return cotizacionRepository.findByVendedorIdOrderByFechaCreacionDesc(vendedorId)
                .stream().map(this::toResumen).toList();
    }

    /* ── Detalle completo ────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public CotizacionDetalleResponse obtenerDetalle(Long id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cotizacion con id " + id));
        return toDetalle(cotizacion);
    }

    /* ── Cliente acepta ──────────────────────────────────────────────────── */

    @Override
    @Transactional
    public CotizacionDetalleResponse aceptar(Long id, Integer clienteId) {
        Cotizacion cotizacion = obtenerYValidarCliente(id, clienteId);
        if (cotizacion.getEstado() != EstadoSolicitud.RESPONDIDA) {
            throw new ConflictoNegocioException("Solo se puede aceptar una cotización en estado RESPONDIDA.");
        }
        cotizacion.aceptar();
        cotizacionRepository.save(cotizacion);
        log.info("Cotización {} aceptada por cliente {}", id, clienteId);
        return toDetalle(cotizacion);
    }

    /* ── Cliente rechaza ─────────────────────────────────────────────────── */

    @Override
    @Transactional
    public void rechazar(Long id, Integer clienteId) {
        Cotizacion cotizacion = obtenerYValidarCliente(id, clienteId);
        if (cotizacion.getEstado() != EstadoSolicitud.RESPONDIDA
                && cotizacion.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ConflictoNegocioException("Solo se puede cancelar una cotización en estado PENDIENTE o RESPONDIDA.");
        }
        cotizacion.cancelar();
        cotizacionRepository.save(cotizacion);
        log.info("Cotización {} cancelada por cliente {}", id, clienteId);
    }

    /* ── Comerciante responde ────────────────────────────────────────────── */

    @Override
    @Transactional
    public CotizacionDetalleResponse responder(Long id, RespuestaCotizacionRequest request, Integer vendedorId) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cotizacion con id " + id));
        if (!vendedorId.equals(cotizacion.getVendedorId())) {
            throw new AccessDeniedException("La cotización no pertenece al comerciante autenticado.");
        }
        if (cotizacion.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ConflictoNegocioException("Solo se puede responder una cotización en estado PENDIENTE.");
        }

        RespuestaSolicitud respuesta = new RespuestaSolicitud();
        respuesta.setIdSolicitud(id);
        respuesta.setPrecioPropuesto(request.getPrecioPropuesto());
        respuesta.setComentario(request.getComentario());
        respuesta.setCondiciones(request.getCondiciones());
        respuesta.setAnotaciones(request.getAnotaciones());
        respuesta.setImagen(request.getImagen());
        respuestaSolicitudRepository.save(respuesta);

        cotizacion.marcarComoRespondida();
        cotizacionRepository.save(cotizacion);
        log.info("Cotización {} respondida por vendedor {}", id, vendedorId);
        return toDetalle(cotizacion);
    }

    /* ── Helpers privados ────────────────────────────────────────────────── */

    private Cotizacion obtenerYValidarCliente(Long id, Integer clienteId) {
        Cotizacion c = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cotizacion con id " + id));
        if (!clienteId.equals(c.getClienteId())) {
            throw new AccessDeniedException("La cotización no pertenece al cliente autenticado.");
        }
        return c;
    }

    private CotizacionResumen toResumen(Cotizacion c) {
        Tienda tienda = c.getIdTienda() != null
                ? tiendaRepository.findById(c.getIdTienda()).orElse(null) : null;
        int cantidad = detalleCotizacionRepository.countByIdCotizacion(c.getId());
        Double precio = respuestaSolicitudRepository.findByIdSolicitud(c.getId())
                .map(RespuestaSolicitud::getPrecioPropuesto).orElse(null);
        Cliente cliente = c.getClienteId() != null
                ? clienteRepository.findById(c.getClienteId()).orElse(null) : null;
        String nombreCliente = cliente != null
                ? trimNombre(cliente.getNombre(), cliente.getApellido()) : null;

        return new CotizacionResumen(
                c.getId(),
                c.getEstado() != null ? c.getEstado().name() : null,
                c.getFechaCreacion() != null ? c.getFechaCreacion().toString() : null,
                tienda != null ? tienda.getIdTienda() : null,
                tienda != null ? tienda.getNombreComercial() : null,
                tienda != null ? tienda.getFoto() : null,
                cantidad,
                precio,
                nombreCliente
        );
    }

    private CotizacionDetalleResponse toDetalle(Cotizacion c) {
        List<DetalleCotizacion> detalles = detalleCotizacionRepository.findByIdCotizacion(c.getId());
        List<CotizacionDetalleResponse.ProductoDetalleInfo> productos = detalles.stream()
                .map(this::toProductoInfo).toList();

        Tienda tienda = c.getIdTienda() != null
                ? tiendaRepository.findById(c.getIdTienda()).orElse(null) : null;
        Cliente cliente = c.getClienteId() != null
                ? clienteRepository.findById(c.getClienteId()).orElse(null) : null;
        String nombreCliente = cliente != null
                ? trimNombre(cliente.getNombre(), cliente.getApellido()) : null;

        RespuestaSolicitud respuesta = respuestaSolicitudRepository.findByIdSolicitud(c.getId()).orElse(null);
        CotizacionDetalleResponse.RespuestaInfo respuestaInfo = toRespuestaInfo(respuesta);

        return new CotizacionDetalleResponse(
                c.getId(),
                c.getEstado() != null ? c.getEstado().name() : null,
                c.getFechaCreacion() != null ? c.getFechaCreacion().toString() : null,
                c.getClienteId(),
                nombreCliente,
                c.getVendedorId(),
                tienda != null ? tienda.getNombreComercial() : null,
                tienda != null ? tienda.getFoto() : null,
                productos,
                respuestaInfo
        );
    }

    private CotizacionDetalleResponse.ProductoDetalleInfo toProductoInfo(DetalleCotizacion detalle) {
        if (detalle instanceof CotizacionCatalogo cc) {
            VarianteProducto variante = cc.getVarianteProducto();
            Producto producto = variante != null ? variante.getProducto() : null;
            String nombre = producto != null ? producto.getNombre() : null;
            String imagenUrl = producto != null ? imagenPrincipal(producto) : null;
            Double precio = variante != null && variante.getPrecioAjustado() != null
                    ? variante.getPrecioAjustado()
                    : (producto != null ? producto.getPrecioBase() : null);
            return new CotizacionDetalleResponse.ProductoDetalleInfo(
                    detalle.getDetalleCotizacionId(), "CATALOGO", nombre, imagenUrl,
                    precio, detalle.getEspecificacion(), detalle.getCantidad());
        }
        ProductoCotizadoManual manual = productoCotizadoManualRepository
                .findByDetalleCotizacionId(detalle.getDetalleCotizacionId()).orElse(null);
        return new CotizacionDetalleResponse.ProductoDetalleInfo(
                detalle.getDetalleCotizacionId(), "MANUAL",
                manual != null ? manual.getNombre() : null,
                manual != null ? manual.getImagen() : null,
                null, detalle.getEspecificacion(), detalle.getCantidad());
    }

    private CotizacionDetalleResponse.RespuestaInfo toRespuestaInfo(RespuestaSolicitud r) {
        if (r == null) return null;
        return new CotizacionDetalleResponse.RespuestaInfo(
                r.getIdRespuesta(), r.getPrecioPropuesto(), r.getComentario(),
                r.getCondiciones(), r.getAnotaciones(), r.getImagen(),
                r.getFecha() != null ? r.getFecha().toString() : null);
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
