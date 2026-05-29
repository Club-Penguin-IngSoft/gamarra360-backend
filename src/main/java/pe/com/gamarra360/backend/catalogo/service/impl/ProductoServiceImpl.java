package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pe.com.gamarra360.backend.catalogo.dto.ImagenRequest;
import pe.com.gamarra360.backend.catalogo.dto.OpcionesFiltroDto;
import pe.com.gamarra360.backend.catalogo.dto.PaginaResponse;
import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.*;
import pe.com.gamarra360.backend.catalogo.repository.*;
import pe.com.gamarra360.backend.catalogo.entity.Color;
import pe.com.gamarra360.backend.catalogo.entity.Talla;
import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;
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
            EspecificacionRepository especificacionRepository) {
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
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "idProducto"));
        var resultado = productoRepository.findByActivoTrue(pageable);
        List<ProductoResponse> contenido = resultado.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PaginaResponse<>(contenido, page, resultado.getTotalPages(), resultado.getTotalElements());
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

        if (comerciante.getIdTienda() == null
                || !comerciante.getIdTienda().equals(request.getIdTienda().longValue())) {
            throw new AccessDeniedException("Solo puedes crear productos en tu propia tienda.");
        }

        Tienda tienda = tiendaRepository.findById(request.getIdTienda())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda no encontrada"));

        if (!Boolean.TRUE.equals(tienda.getVerificada())) {
            throw new DatosInvalidosException("La tienda no está verificada para publicar productos.");
        }

        Categoria cat = resolverCategoria(request.getIdCategoria());
        TipoProducto tipo = resolverTipoProducto(request.getIdTipoProducto());

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setTienda(tienda);
        producto.setActivo(true);
        producto.setCategoria(cat);
        producto.setTipoProducto(tipo);

        Producto saved = productoRepository.save(producto);
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), saved);

        return buildResponse(saved, tienda.getNombreComercial(), cat, tipo, savedImages, List.of());
    }

    @Override
    public ProductoResponse actualizarProducto(Integer idProducto, ProductoRequest request, Integer comercianteId) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto);
        }

        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado"));

        Integer tiendaIdProducto = producto.getIdTienda();
        if (comerciante.getIdTienda() == null
                || tiendaIdProducto == null
                || !comerciante.getIdTienda().equals(tiendaIdProducto.longValue())) {
            throw new AccessDeniedException("No tienes permiso para editar productos de otra tienda.");
        }

        Categoria cat = resolverCategoria(request.getIdCategoria());
        TipoProducto tipo = resolverTipoProducto(request.getIdTipoProducto());

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setCategoria(cat);
        producto.setTipoProducto(tipo);

        imagenProductoRepository.deleteAll(imagenProductoRepository.findByIdProducto(idProducto));
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), producto);

        Tienda tienda = producto.getTienda();
        List<VarianteProducto> variantes = varianteProductoRepository.findByIdProducto(idProducto);

        productoRepository.save(producto);

        return buildResponse(producto, tienda != null ? tienda.getNombreComercial() : null,
                cat, tipo, savedImages, variantes);
    }

    @Override
    public void eliminarProducto(Integer idProducto, Integer comercianteId) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));

        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado"));

        Integer tiendaIdProducto = producto.getIdTienda();
        if (comerciante.getIdTienda() == null
                || tiendaIdProducto == null
                || !comerciante.getIdTienda().equals(tiendaIdProducto.longValue())) {
            throw new AccessDeniedException("No tienes permiso para eliminar productos de otra tienda.");
        }

        boolean tienePedidos = detallePedidoRepository.existePedidoActivoPorProducto(
                idProducto,
                List.of(EstadoPedido.PENDIENTE_CONFIRMACION, EstadoPedido.EN_PREPARACION));

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
    public java.util.Map<String, List<ProductoResponse>> listarDestacados(int porCategoria) {
        // Una sola query para traer todos los productos activos
        List<Producto> todos = productoRepository.findByActivoTrue();

        // Agrupar por nombre de categoría, tomar los N más recientes (ID desc) por grupo
        java.util.Map<String, List<Producto>> porCat = todos.stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(p -> p.getCategoria().getNombreCategoria()));

        java.util.Map<String, List<ProductoResponse>> resultado = new java.util.LinkedHashMap<>();
        porCat.forEach((cat, prods) -> {
            List<ProductoResponse> top = prods.stream()
                    .sorted(java.util.Comparator.comparing(Producto::getIdProducto).reversed())
                    .limit(porCategoria)
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            resultado.put(cat, top);
        });

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionesFiltroDto obtenerOpcionesFiltro() {
        // Colores activos ordenados alfabéticamente
        List<String> colores = colorRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .map(Color::getNombre)
                .sorted()
                .collect(Collectors.toList());

        // Materiales distintos desde la tabla especificaciones
        List<String> materiales = especificacionRepository.findDistinctMateriales();

        // Tallas activas en el orden que vienen de la BD (orden de inserción / id)
        List<String> tallas = tallaRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .map(Talla::getTalla)
                .collect(Collectors.toList());

        // Tipos de producto ordenados alfabéticamente — distinct() evita duplicados
        // porque la tabla tiene una fila por cada combinación tipo+categoría
        // (ej. "Polos" para Hombre, "Polos" para Mujer → un solo "Polos" en el filtro)
        List<String> tiposProducto = tipoProductoRepository.findAll().stream()
                .map(TipoProducto::getNombre)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return new OpcionesFiltroDto(colores, materiales, tallas, tiposProducto);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Aplica el mejor descuento por volumen activo (cantidad_minima más baja)
     * para mostrar el precio "desde" en el catálogo. Devuelve precioBase si no
     * hay descuentos activos, y null si precioBase es null.
     */
    private Double calcularPrecioFinal(Double precioBase, List<DescuentoVolumen> descuentos) {
        if (precioBase == null) return null;
        if (descuentos == null || descuentos.isEmpty()) return precioBase;
        return descuentos.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .min(java.util.Comparator.comparing(DescuentoVolumen::getCantidadMinima))
                .map(d -> precioBase * (1.0 - d.getPorcentajeDescuento() / 100.0))
                .orElse(precioBase);
    }

    private Categoria resolverCategoria(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new DatosInvalidosException(
                        "La categoria no existe con id: " + idCategoria));
    }

    private TipoProducto resolverTipoProducto(Integer idTipoProducto) {
        if (idTipoProducto == null) return null;
        return tipoProductoRepository.findById(idTipoProducto)
                .orElseThrow(() -> new DatosInvalidosException(
                        "El tipo de producto no existe con id: " + idTipoProducto));
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
        List<VarianteProducto> vars = varianteProductoRepository.findByIdProducto(p.getIdProducto());
        Tienda tienda = p.getTienda();
        return buildResponse(p, tienda != null ? tienda.getNombreComercial() : null,
                p.getCategoria(), p.getTipoProducto(), imgs, vars);
    }

    private ProductoResponse buildResponse(Producto p, String nombreTienda,
                                            Categoria cat, TipoProducto tipo,
                                            List<ImagenProducto> imgs,
                                            List<VarianteProducto> vars) {
        ProductoResponse r = new ProductoResponse();
        r.setIdProducto(p.getIdProducto());
        r.setNombre(p.getNombre());
        r.setDescripcion(p.getDescripcion());
        r.setPrecioBase(p.getPrecioBase());
        r.setPrecioFinal(calcularPrecioFinal(p.getPrecioBase(), p.getDescuentosVolumen()));
        r.setEsPersonalizable(p.getEsPersonalizable());
        r.setActivo(p.getActivo());
        r.setIdTienda(p.getIdTienda());
        r.setNombreTienda(nombreTienda);

        // Categoria como lista de un elemento (compat con frontend)
        if (cat != null) {
            ProductoResponse.CategoriaDto d = new ProductoResponse.CategoriaDto();
            d.setIdCategoria(cat.getIdCategoria());
            d.setNombre(cat.getNombreCategoria());
            r.setCategorias(List.of(d));
        } else {
            r.setCategorias(List.of());
        }

        // Tipo de producto
        if (tipo != null) {
            ProductoResponse.TipoProductoDto tp = new ProductoResponse.TipoProductoDto();
            tp.setIdTipoProducto(tipo.getIdTipoProducto());
            tp.setNombre(tipo.getNombre());
            r.setTipoProducto(tp);
        }

        // Especificaciones (Material, Tejido, etc.)
        r.setEspecificaciones(p.getEspecificaciones().stream().map(e -> {
            ProductoResponse.EspecificacionDto d = new ProductoResponse.EspecificacionDto();
            d.setNombre(e.getNombre());
            d.setDescripcion(e.getDescripcion());
            return d;
        }).collect(Collectors.toList()));

        r.setImagenes(imgs.stream().map(i -> {
            ProductoResponse.ImagenDto d = new ProductoResponse.ImagenDto();
            d.setIdImagen(i.getIdImagen());
            d.setUrl(i.getUrl());
            d.setEsPrincipal(i.getEsPrincipal());
            return d;
        }).collect(Collectors.toList()));

        r.setVariantes(vars.stream().map(v -> {
            ProductoResponse.VarianteDto d = new ProductoResponse.VarianteDto();
            d.setIdVariante(v.getIdVariante());
            d.setSku(v.getSku());
            d.setStock(v.getStock());
            d.setPrecioAjustado(v.getPrecioAjustado());
            d.setDisponible(v.getDisponible());
            d.setTalla(v.getTalla() != null ? v.getTalla().getTalla() : null);
            d.setColor(v.getColor() != null ? v.getColor().getNombre() : null);
            d.setColorHex(v.getColor() != null ? v.getColor().getCodHex() : null);
            return d;
        }).collect(Collectors.toList()));

        return r;
    }
}
