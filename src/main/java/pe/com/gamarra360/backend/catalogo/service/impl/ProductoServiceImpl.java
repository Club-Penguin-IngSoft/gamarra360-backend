package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import pe.com.gamarra360.backend.catalogo.dto.ImagenRequest;
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
    private final ImagenProductoRepository imagenProductoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CotizacionCatalogoRepository cotizacionCatalogoRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            ComercianteRepository comercianteRepository,
            TiendaRepository tiendaRepository,
            CategoriaRepository categoriaRepository,
            ImagenProductoRepository imagenProductoRepository,
            VarianteProductoRepository varianteProductoRepository,
            DetallePedidoRepository detallePedidoRepository,
            CotizacionCatalogoRepository cotizacionCatalogoRepository) {
        super(productoRepository, "Producto");
        this.productoRepository = productoRepository;
        this.comercianteRepository = comercianteRepository;
        this.tiendaRepository = tiendaRepository;
        this.categoriaRepository = categoriaRepository;
        this.imagenProductoRepository = imagenProductoRepository;
        this.varianteProductoRepository = varianteProductoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.cotizacionCatalogoRepository = cotizacionCatalogoRepository;
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

        List<Categoria> cats = resolverCategorias(request.getIdCategorias());

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setTienda(tienda);
        producto.setActivo(true);
        producto.setCategorias(cats);

        Producto saved = productoRepository.save(producto);
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), saved);

        return buildResponse(saved, tienda.getNombreComercial(), cats, savedImages, List.of());
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

        List<Categoria> cats = resolverCategorias(request.getIdCategorias());

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setCategorias(cats);

        imagenProductoRepository.deleteAll(imagenProductoRepository.findByIdProducto(idProducto));
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), producto);

        Tienda tienda = producto.getTienda();
        List<VarianteProducto> variantes = varianteProductoRepository.findByIdProducto(idProducto);

        productoRepository.save(producto);

        return buildResponse(producto, tienda != null ? tienda.getNombreComercial() : null,
                cats, savedImages, variantes);
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

    // ── helpers ───────────────────────────────────────────────────────────────

    private List<Categoria> resolverCategorias(List<Integer> ids) {
        List<Categoria> cats = categoriaRepository.findAllById(ids);
        if (new HashSet<>(cats).size() != ids.size()) {
            throw new DatosInvalidosException("Una o más categorías no existen.");
        }
        return cats;
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
                p.getCategorias(), imgs, vars);
    }

    private ProductoResponse buildResponse(Producto p, String nombreTienda,
                                            List<Categoria> cats, List<ImagenProducto> imgs,
                                            List<VarianteProducto> vars) {
        ProductoResponse r = new ProductoResponse();
        r.setIdProducto(p.getIdProducto());
        r.setNombre(p.getNombre());
        r.setDescripcion(p.getDescripcion());
        r.setPrecioBase(p.getPrecioBase());
        r.setEsPersonalizable(p.getEsPersonalizable());
        r.setActivo(p.getActivo());
        r.setIdTienda(p.getIdTienda());
        r.setNombreTienda(nombreTienda);

        r.setCategorias(cats.stream().map(c -> {
            ProductoResponse.CategoriaDto d = new ProductoResponse.CategoriaDto();
            d.setIdCategoria(c.getIdCategoria());
            d.setNombre(c.getNombreCategoria());
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
            d.setIdTalla(v.getTalla() != null ? v.getTalla().getIdTalla() : null);
            d.setIdColor(v.getColor() != null ? v.getColor().getIdColor() : null);
            return d;
        }).collect(Collectors.toList()));

        return r;
    }
}
