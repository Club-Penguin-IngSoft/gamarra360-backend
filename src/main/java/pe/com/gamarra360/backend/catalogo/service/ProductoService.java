package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.gamarra360.backend.catalogo.dto.ImagenRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.*;
import pe.com.gamarra360.backend.catalogo.repository.*;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.enums.EstadoSolicitud;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionCatalogoRepository;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ComercianteRepository comercianteRepository;
    private final TiendaRepository tiendaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ImagenProductoRepository imagenProductoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CotizacionCatalogoRepository cotizacionCatalogoRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            ComercianteRepository comercianteRepository,
            TiendaRepository tiendaRepository,
            CategoriaRepository categoriaRepository,
            ImagenProductoRepository imagenProductoRepository,
            VarianteProductoRepository varianteProductoRepository,
            DetallePedidoRepository detallePedidoRepository,
            CotizacionCatalogoRepository cotizacionCatalogoRepository) {
        this.productoRepository = productoRepository;
        this.comercianteRepository = comercianteRepository;
        this.tiendaRepository = tiendaRepository;
        this.categoriaRepository = categoriaRepository;
        this.imagenProductoRepository = imagenProductoRepository;
        this.varianteProductoRepository = varianteProductoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.cotizacionCatalogoRepository = cotizacionCatalogoRepository;
    }

    public List<Producto> listar() {
        log.info("Listando Producto");
        return productoRepository.findAll();
    }

    public Producto obtener(Integer id) {
        log.info("Obteniendo Producto con id {}", id);
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    public Producto crear(Producto entidad) {
        log.info("Creando Producto");
        return productoRepository.save(entidad);
    }

    public Producto actualizar(Integer id, Producto entidad) {
        log.info("Actualizando Producto con id {}", id);
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id " + id);
        }
        entidad.setIdProducto(id);
        return productoRepository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Producto con id {}", id);
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id " + id);
        }
        productoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodosComoResponse() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorTienda(Integer idTienda) {
        return productoRepository.findByIdTiendaAndActivoTrue(idTienda).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoResponse(Integer idProducto) {
        Producto p = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));
        return toResponse(p);
    }

    public ProductoResponse crearProducto(ProductoRequest request, Integer comercianteId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado"));

        if (!Boolean.TRUE.equals(comerciante.getVerificado())) {
            throw new DatosInvalidosException(
                    "Tu cuenta de comerciante no esta verificada. Contacta al administrador.");
        }

        if (comerciante.getIdTienda() == null
                || !comerciante.getIdTienda().equals(request.getIdTienda().longValue())) {
            throw new AccessDeniedException("Solo puedes crear productos en tu propia tienda.");
        }

        Tienda tienda = tiendaRepository.findById(request.getIdTienda())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda no encontrada"));

        if (!Boolean.TRUE.equals(tienda.getVerificada())) {
            throw new DatosInvalidosException("La tienda no esta verificada para publicar productos.");
        }

        Set<Categoria> cats = resolverCategorias(request.getIdCategorias());

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setIdTienda(request.getIdTienda());
        producto.setActivo(true);
        producto.setCategorias(cats);

        Producto saved = productoRepository.save(producto);
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), saved.getIdProducto());

        return buildResponse(saved, tienda.getNombreComercial(), new ArrayList<>(cats), savedImages, List.of());
    }

    public ProductoResponse actualizarProducto(Integer idProducto, ProductoRequest request, Integer comercianteId) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto);
        }

        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado"));

        if (comerciante.getIdTienda() == null
                || !comerciante.getIdTienda().equals(producto.getIdTienda().longValue())) {
            throw new AccessDeniedException("No tienes permiso para editar productos de otra tienda.");
        }

        Set<Categoria> cats = resolverCategorias(request.getIdCategorias());

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsPersonalizable(Boolean.TRUE.equals(request.getEsPersonalizable()));
        producto.setCategorias(cats);

        imagenProductoRepository.deleteAll(imagenProductoRepository.findByIdProducto(idProducto));
        List<ImagenProducto> savedImages = guardarImagenes(request.getImagenes(), idProducto);

        Tienda tienda = tiendaRepository.findById(producto.getIdTienda()).orElse(null);
        List<VarianteProducto> variantes = varianteProductoRepository.findByIdProducto(idProducto);

        productoRepository.save(producto);

        return buildResponse(producto, tienda != null ? tienda.getNombreComercial() : null,
                new ArrayList<>(cats), savedImages, variantes);
    }

    public void eliminarProducto(Integer idProducto, Integer comercianteId) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + idProducto));

        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado"));

        if (comerciante.getIdTienda() == null
                || !comerciante.getIdTienda().equals(producto.getIdTienda().longValue())) {
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

    private Set<Categoria> resolverCategorias(List<Integer> ids) {
        Set<Categoria> cats = new HashSet<>(categoriaRepository.findAllById(ids));
        if (cats.size() != ids.size()) {
            throw new DatosInvalidosException("Una o mas categorias no existen.");
        }
        return cats;
    }

    private List<ImagenProducto> guardarImagenes(List<ImagenRequest> imagenes, Integer idProducto) {
        List<ImagenProducto> result = new ArrayList<>();
        for (ImagenRequest imgReq : imagenes) {
            ImagenProducto img = new ImagenProducto();
            img.setUrl(imgReq.getUrl());
            img.setEsPrincipal(Boolean.TRUE.equals(imgReq.getEsPrincipal()));
            img.setIdProducto(idProducto);
            result.add(imagenProductoRepository.save(img));
        }
        return result;
    }

    private ProductoResponse toResponse(Producto p) {
        List<ImagenProducto> imgs = imagenProductoRepository.findByIdProducto(p.getIdProducto());
        List<VarianteProducto> vars = varianteProductoRepository.findByIdProducto(p.getIdProducto());
        Tienda tienda = p.getIdTienda() != null
                ? tiendaRepository.findById(p.getIdTienda()).orElse(null) : null;
        return buildResponse(p, tienda != null ? tienda.getNombreComercial() : null,
                new ArrayList<>(p.getCategorias()), imgs, vars);
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
            d.setIdTalla(v.getIdTalla());
            d.setIdColor(v.getIdColor());
            return d;
        }).collect(Collectors.toList()));

        return r;
    }
}
