package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import pe.com.gamarra360.backend.catalogo.dto.EspecificacionProductoDto;
import pe.com.gamarra360.backend.catalogo.dto.FiltrosCatalogoDto;
import pe.com.gamarra360.backend.catalogo.dto.ImagenRequest;
import pe.com.gamarra360.backend.catalogo.dto.OfertaResumenDto;
import pe.com.gamarra360.backend.catalogo.dto.OpcionesFiltroDto;
import pe.com.gamarra360.backend.catalogo.dto.PaginaResponse;
import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.*;
import pe.com.gamarra360.backend.catalogo.repository.*;
import pe.com.gamarra360.backend.catalogo.service.ProductoService;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.enums.EstadoSolicitud;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionCatalogoRepository;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ProductoServiceImpl extends AbstractCrudService<Producto, Integer> implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ComercianteRepository comercianteRepository;
    private final TiendaRepository tiendaRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final ImagenProductoRepository imagenProductoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CotizacionCatalogoRepository cotizacionCatalogoRepository;
    private final ColorRepository colorRepository;
    private final TallaRepository tallaRepository;
    private final EspecificacionRepository especificacionRepository;
    private final MaterialFiltroRepository materialFiltroRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            ComercianteRepository comercianteRepository,
            TiendaRepository tiendaRepository,
            CategoriaRepository categoriaRepository,
            TipoProductoRepository tipoProductoRepository,
            ImagenProductoRepository imagenProductoRepository,
            VarianteProductoRepository varianteProductoRepository,
            DetallePedidoRepository detallePedidoRepository,
            CotizacionCatalogoRepository cotizacionCatalogoRepository,
            ColorRepository colorRepository,
            TallaRepository tallaRepository,
            EspecificacionRepository especificacionRepository,
            MaterialFiltroRepository materialFiltroRepository) {
        super(productoRepository, "Producto");
        this.productoRepository = productoRepository;
        this.comercianteRepository = comercianteRepository;
        this.tiendaRepository = tiendaRepository;
        this.categoriaRepository = categoriaRepository;
        this.tipoProductoRepository = tipoProductoRepository;
        this.imagenProductoRepository = imagenProductoRepository;
        this.varianteProductoRepository = varianteProductoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.cotizacionCatalogoRepository = cotizacionCatalogoRepository;
        this.colorRepository = colorRepository;
        this.tallaRepository = tallaRepository;
        this.especificacionRepository = especificacionRepository;
        this.materialFiltroRepository = materialFiltroRepository;
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(Producto entidad, Integer id) { entidad.setIdProducto(id); }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodosComoResponse() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ProductoResponse> listarPaginado(int page, int size) {
        var pageable = PageRequest.of(page, size);
        var resultado = productoRepository.findByActivoTrue(pageable);
        List<ProductoResponse> contenido = resultado.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PaginaResponse<>(contenido, page, resultado.getTotalPages(), resultado.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionesFiltroDto obtenerOpcionesFiltro() {
        List<String> colores = colorRepository.findAll().stream()
                .map(c -> c.getNombre())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> tallas = tallaRepository.findAll().stream()
                .map(t -> t.getTalla())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> materiales = especificacionRepository.findDistinctMateriales();

        List<String> tiposProducto = tipoProductoRepository.findAll().stream()
                .map(TipoProducto::getNombre)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> categorias = categoriaRepository.findAll().stream()
                .map(c -> c.getNombreCategoria())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return new OpcionesFiltroDto(colores, materiales, tallas, tiposProducto, categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<ProductoResponse>> listarDestacados(int porCategoria) {
        return productoRepository.findByActivoTrue().stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getNombreCategoria(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                lista -> lista.stream()
                                        .sorted(Comparator.comparing(Producto::getIdProducto).reversed())
                                        .limit(porCategoria)
                                        .map(this::toResponse)
                                        .collect(Collectors.toList())
                        )
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorTienda(Integer idTienda) {
        return productoRepository.findByIdTiendaAndActivoTrue(idTienda).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoResponse(Integer idProducto) {
        Producto p = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));
        return toResponse(p);
    }

    @Override
    public ProductoResponse crearProducto(ProductoRequest request, Integer comercianteId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado"));

        if (!Boolean.TRUE.equals(comerciante.getVerificado())) {
            throw new DatosInvalidosException(
                    "Tu cuenta de comerciante no está verificada. Contacta al administrador.");
        }

        Tienda tienda = tiendaRepository.findByIdComerciante(comercianteId)
                .orElseThrow(() -> new DatosInvalidosException("No tienes una tienda asignada."));

        if (!Boolean.TRUE.equals(tienda.getVerificada())) {
            throw new DatosInvalidosException("La tienda no está verificada para publicar productos.");
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + request.getIdCategoria()));

        TipoProducto tipoProducto = tipoProductoRepository.findById(request.getIdTipoProducto())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de producto no encontrado con id " + request.getIdTipoProducto()));

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setTienda(tienda);
        producto.setActivo(true);
        producto.setCategoria(categoria);
        producto.setTipoProducto(tipoProducto);
        if (request.getIdMaterialFiltro() != null) {
            producto.setMaterialFiltro(materialFiltroRepository.findById(request.getIdMaterialFiltro())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Material no encontrado con id " + request.getIdMaterialFiltro())));
        }

        Producto saved = productoRepository.save(producto);
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), saved);
        List<Especificacion> savedEspecs = guardarEspecificaciones(request.getEspecificaciones(), saved);

        return buildResponse(saved, tienda.getNombreComercial(), savedImages, savedEspecs, List.of());
    }

    @Override
    public ProductoResponse actualizarProducto(Integer idProducto, ProductoRequest request, Integer comercianteId) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto);
        }

        Tienda tiendaComerciante = tiendaRepository.findByIdComerciante(comercianteId)
                .orElseThrow(() -> new DatosInvalidosException("No tienes una tienda asignada."));

        Integer tiendaIdProducto = producto.getIdTienda();
        if (!tiendaComerciante.getIdTienda().equals(tiendaIdProducto)) {
            throw new AccessDeniedException("No tienes permiso para editar productos de otra tienda.");
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + request.getIdCategoria()));

        TipoProducto tipoProducto = tipoProductoRepository.findById(request.getIdTipoProducto())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de producto no encontrado con id " + request.getIdTipoProducto()));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setCategoria(categoria);
        producto.setTipoProducto(tipoProducto);
        if (request.getIdMaterialFiltro() != null) {
            producto.setMaterialFiltro(materialFiltroRepository.findById(request.getIdMaterialFiltro())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Material no encontrado con id " + request.getIdMaterialFiltro())));
        } else {
            producto.setMaterialFiltro(null);
        }

        imagenProductoRepository.deleteAll(imagenProductoRepository.findByIdProducto(idProducto));
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), producto);

        especificacionRepository.deleteByIdProducto(idProducto);
        List<Especificacion> savedEspecs = guardarEspecificaciones(request.getEspecificaciones(), producto);

        Tienda tienda = producto.getTienda();
        List<VarianteProducto> variantes = varianteProductoRepository.findByIdProducto(idProducto);

        productoRepository.save(producto);

        return buildResponse(producto, tienda != null ? tienda.getNombreComercial() : null,
                savedImages, savedEspecs, variantes);
    }

    @Override
    public void eliminarProducto(Integer idProducto, Integer comercianteId) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));

        Tienda tiendaComerciante = tiendaRepository.findByIdComerciante(comercianteId)
                .orElseThrow(() -> new DatosInvalidosException("No tienes una tienda asignada."));

        Integer tiendaIdProducto = producto.getIdTienda();
        if (!tiendaComerciante.getIdTienda().equals(tiendaIdProducto)) {
            throw new AccessDeniedException("No tienes permiso para eliminar productos de otra tienda.");
        }

        boolean tienePedidos = detallePedidoRepository.existePedidoActivoPorProducto(
                idProducto,
                List.of(EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION, EstadoPedido.EN_CAMINO, EstadoPedido.LISTO_PARA_ENTREGA));

        boolean tieneCotizaciones = cotizacionCatalogoRepository.existeCotizacionActivaPorProducto(
                idProducto,
                List.of(EstadoSolicitud.PENDIENTE, EstadoSolicitud.RESPONDIDA));

        if (tienePedidos || tieneCotizaciones) {
            String razon = tienePedidos ? "pedidos activos" : "cotizaciones en curso";
            throw new ConflictoNegocioException(
                    "No se puede eliminar el producto porque tiene " + razon
                            + ". Espera a que se completen antes de eliminarlo.");
        }

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarPorKeyword(String q, int size) {
        return productoRepository.buscarPorKeyword(q).stream()
                .limit(size)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ProductoResponse> listarConFiltros(FiltrosCatalogoDto filtros) {
        int page = filtros.getPage() != null ? filtros.getPage() : 0;
        int size = filtros.getSize() != null ? filtros.getSize() : 12;
        Sort sort = resolverOrden(filtros.getSort());
        var pageable = PageRequest.of(page, size, sort);

        var resultado = productoRepository.findAll(buildSpec(filtros), pageable);
        List<ProductoResponse> contenido = resultado.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PaginaResponse<>(contenido, page, resultado.getTotalPages(), resultado.getTotalElements());
    }

    private Sort resolverOrden(String sort) {
        if ("PRICE_ASC".equals(sort))  return Sort.by(Sort.Direction.ASC,  "precioBase");
        if ("PRICE_DESC".equals(sort)) return Sort.by(Sort.Direction.DESC, "precioBase");
        return Sort.by(Sort.Direction.DESC, "idProducto");
    }

    private Specification<Producto> buildSpec(FiltrosCatalogoDto f) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            // Visibilidad del catálogo público
            Join<Producto, ?> tienda = root.join("tienda", JoinType.LEFT);
            Join<?, ?>  comerciante  = tienda.join("comerciante", JoinType.LEFT);
            predicados.add(cb.isTrue(root.get("activo")));
            predicados.add(cb.isTrue(tienda.get("verificada")));
            predicados.add(cb.isTrue(comerciante.get("verificado")));
            predicados.add(cb.isTrue(comerciante.get("activo")));

            // Tipos de producto (ManyToOne → sin duplicados)
            if (f.getTiposProducto() != null && !f.getTiposProducto().isEmpty()) {
                Join<Producto, ?> tp = root.join("tipoProducto", JoinType.LEFT);
                predicados.add(tp.get("nombre").in(f.getTiposProducto()));
            }

            // Categorías (ManyToOne → sin duplicados)
            if (f.getCategorias() != null && !f.getCategorias().isEmpty()) {
                Join<Producto, ?> cat = root.join("categoria", JoinType.LEFT);
                predicados.add(cat.get("nombreCategoria").in(f.getCategorias()));
            }

            // Búsqueda por keyword
            if (f.getQ() != null && !f.getQ().isBlank()) {
                String like = "%" + f.getQ().toLowerCase() + "%";
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")),             like),
                        cb.like(cb.lower(root.get("descripcion")),        like),
                        cb.like(cb.lower(tienda.get("nombreComercial")),  like)
                ));
            }

            // Rango de precio
            if (f.getPrecioMin() != null)
                predicados.add(cb.greaterThanOrEqualTo(root.get("precioBase"), f.getPrecioMin()));
            if (f.getPrecioMax() != null)
                predicados.add(cb.lessThanOrEqualTo(root.get("precioBase"), f.getPrecioMax()));

            // Color — subquery para evitar duplicados por OneToMany
            if (f.getColor() != null && !f.getColor().isBlank()) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                Root<VarianteProducto> vRoot = sub.from(VarianteProducto.class);
                Join<?, ?> colorJoin = vRoot.join("color", JoinType.LEFT);
                sub.select(vRoot.get("idVariante")).where(cb.and(
                        cb.equal(vRoot.get("producto"), root),
                        cb.equal(cb.lower(colorJoin.get("nombre")), f.getColor().toLowerCase())
                ));
                predicados.add(cb.exists(sub));
            }

            // Tallas — subquery para evitar duplicados por OneToMany
            if (f.getTallas() != null && !f.getTallas().isEmpty()) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                Root<VarianteProducto> vRoot = sub.from(VarianteProducto.class);
                Join<?, ?> tallaJoin = vRoot.join("talla", JoinType.LEFT);
                sub.select(vRoot.get("idVariante")).where(cb.and(
                        cb.equal(vRoot.get("producto"), root),
                        tallaJoin.get("talla").in(f.getTallas())
                ));
                predicados.add(cb.exists(sub));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Double calcularPrecioFinal(Double precioBase, List<DescuentoVolumen> descuentos) {
        if (precioBase == null) return null;
        if (descuentos == null || descuentos.isEmpty()) return precioBase;
        return descuentos.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .min(Comparator.comparing(DescuentoVolumen::getCantidadMinima))
                .map(d -> precioBase * (1.0 - d.getPorcentajeDescuento() / 100.0))
                .orElse(precioBase);
    }

    private boolean esOfertaActiva(Oferta oferta) {
        if (oferta == null || !Boolean.TRUE.equals(oferta.getActiva())) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(oferta.getFechaInicio()) && !now.isAfter(oferta.getFechaFin());
    }

    private Double calcularPrecioConOferta(Double precioBase, Oferta oferta) {
        if (precioBase == null) return null;
        return switch (oferta.getTipoDescuento()) {
            case PORCENTAJE -> precioBase - (precioBase * (oferta.getValorDescuento() / 100.0));
            case MONTO_FIJO -> Math.max(0.0, precioBase - oferta.getValorDescuento());
        };
    }

    private List<ImagenProducto> guardarImagenes(List<ImagenRequest> imagenes, Producto producto) {
        List<ImagenProducto> result = new ArrayList<>();
        for (ImagenRequest imgReq : imagenes) {
            ImagenProducto img = new ImagenProducto();
            img.setUrl(imgReq.getUrl());
            img.setEsPrincipal(Boolean.TRUE.equals(imgReq.getEsPrincipal()));
            img.setProducto(producto);
            result.add(imagenProductoRepository.save(img));
        }
        return result;
    }

    private ProductoResponse toResponse(Producto p) {
        List<ImagenProducto> imgs = imagenProductoRepository.findByIdProducto(p.getIdProducto());
        List<Especificacion> especs = especificacionRepository.findByIdProducto(p.getIdProducto());
        List<VarianteProducto> vars = varianteProductoRepository.findByIdProducto(p.getIdProducto());
        Tienda tienda = p.getTienda();
        return buildResponse(p, tienda != null ? tienda.getNombreComercial() : null, imgs, especs, vars);
    }

    private List<Especificacion> guardarEspecificaciones(List<EspecificacionProductoDto> dtos, Producto producto) {
        if (dtos == null || dtos.isEmpty()) return List.of();
        List<Especificacion> result = new ArrayList<>();
        for (EspecificacionProductoDto dto : dtos) {
            Especificacion esp = new Especificacion();
            esp.setNombre(dto.getNombre());
            esp.setDescripcion(dto.getDescripcion());
            esp.setIdProducto(producto.getIdProducto());
            result.add(especificacionRepository.save(esp));
        }
        return result;
    }

    private ProductoResponse buildResponse(Producto p, String nombreTienda,
                                            List<ImagenProducto> imgs,
                                            List<Especificacion> especs,
                                            List<VarianteProducto> vars) {
        ProductoResponse r = new ProductoResponse();
        r.setIdProducto(p.getIdProducto());
        r.setNombre(p.getNombre());
        r.setDescripcion(p.getDescripcion());
        r.setPrecioBase(p.getPrecioBase());

        Oferta oferta = p.getOferta();
        if (esOfertaActiva(oferta)) {
            r.setPrecioFinal(calcularPrecioConOferta(p.getPrecioBase(), oferta));
            r.setOferta(new OfertaResumenDto(oferta.getTitulo(), oferta.getTipoDescuento(), oferta.getValorDescuento()));
        } else {
            r.setPrecioFinal(calcularPrecioFinal(p.getPrecioBase(), p.getDescuentosVolumen()));
            r.setOferta(null);
        }

        r.setEsPersonalizable(p.getEsPersonalizable());
        r.setActivo(p.getActivo());
        r.setIdTienda(p.getIdTienda());
        r.setIdComerciante(p.getTienda() != null ? p.getTienda().getIdComerciante() : null);
        r.setNombreTienda(nombreTienda);

        if (p.getCategoria() != null) {
            r.setIdCategoria(p.getCategoria().getIdCategoria());
            r.setNombreCategoria(p.getCategoria().getNombreCategoria());
        }
        if (p.getTipoProducto() != null) {
            r.setIdTipoProducto(p.getTipoProducto().getIdTipoProducto());
            r.setNombreTipoProducto(p.getTipoProducto().getNombre());
        }

        r.setImagenes(imgs.stream().map(i -> {
            ProductoResponse.ImagenDto d = new ProductoResponse.ImagenDto();
            d.setIdImagen(i.getIdImagen());
            d.setUrl(i.getUrl());
            d.setEsPrincipal(i.getEsPrincipal());
            return d;
        }).collect(Collectors.toList()));

        r.setMaterialPrincipal(p.getMaterialFiltro() != null ? p.getMaterialFiltro().getNombre() : null);

        r.setMateriales(especs.stream()
                .filter(e -> "material".equalsIgnoreCase(e.getNombre()))
                .map(Especificacion::getDescripcion)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        r.setTiendaOfreceEnvio(p.getTienda() != null ? p.getTienda().getOfreceEnvioDomicilio() : null);

        r.setEspecificaciones(especs.stream().map(e -> {
            ProductoResponse.EspecificacionDto d = new ProductoResponse.EspecificacionDto();
            d.setIdEspecificacion(e.getIdEspecificacion());
            d.setNombre(e.getNombre());
            d.setDescripcion(e.getDescripcion());
            return d;
        }).collect(Collectors.toList()));

        r.setVariantes(vars.stream().map(v -> {
            ProductoResponse.VarianteDto d = new ProductoResponse.VarianteDto();
            d.setIdVariante(v.getIdVariante());
            d.setSku(v.getSku());
            d.setStock(v.getStock());
            d.setPrecioAjustado(v.getPrecioAjustado());
            d.setDisponible(v.getDisponible());
            d.setIdTalla(v.getTalla() != null ? v.getTalla().getIdTalla() : null);
            d.setIdColor(v.getColor() != null ? v.getColor().getIdColor() : null);
            d.setTalla(v.getTalla() != null ? v.getTalla().getTalla() : null);
            d.setColor(v.getColor() != null ? v.getColor().getNombre() : null);
            d.setColorHex(v.getColor() != null ? v.getColor().getCodHex() : null);
            d.setImagenUrl(v.getImagenUrl());
            return d;
        }).collect(Collectors.toList()));

        return r;
    }
}
