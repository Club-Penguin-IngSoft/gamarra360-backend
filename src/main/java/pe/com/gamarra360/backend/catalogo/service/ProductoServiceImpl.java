package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.FiltrosCatalogoDto;
import pe.com.gamarra360.backend.catalogo.dto.PagedResponse;
import pe.com.gamarra360.backend.catalogo.dto.ProductoDto;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.mapper.ProductoMapper;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Random;

/**
 * Implementación del servicio de productos.
 *
 * Estrategia de búsqueda por relevancia (CU-08, RF-22):
 *  - El repository hace un primer filtro grueso por LIKE en BD (rápido,
 *    usa índices si los hay).
 *  - El service aplica un scoring fine-grained en memoria para ranquear.
 *  - Migración futura: MATCH AGAINST con FULLTEXT INDEX en MySQL.
 */
@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    /* Pesos del scoring de relevancia — alineados con el frontend mock */
    private static final int PESO_TITULO       = 10;
    private static final int PESO_DESCRIPCION  = 5;
    private static final int PESO_TIENDA       = 5;
    private static final int PESO_CATEGORIA    = 3;

    /**
     * Inyección por constructor (CLAUDE.md §4 — preferido sobre @Autowired field).
     */
    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    /* =========================================================================
       Listado con filtros (CU-07 + CU-08)
       ========================================================================= */

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductoDto> listarConFiltros(FiltrosCatalogoDto filtros) {
        if (filtros == null) filtros = new FiltrosCatalogoDto();

        // 1) Obtener candidatos desde la BD (ya con exclusión de comerciantes no aprobados)
        List<Producto> candidatos;
        String q = filtros.getQ();
        boolean hayBusqueda = q != null && !q.trim().isEmpty();

        if (hayBusqueda) {
            // LIKE en BD → reduce el set antes de scoring en memoria
            candidatos = productoRepository.buscarPorKeyword(q.trim());
            log.debug("Búsqueda '{}' devolvió {} candidatos antes del scoring", q, candidatos.size());
        } else {
            candidatos = productoRepository.findCatalogoPublico();
        }

        // 2) Aplicar filtros estructurados en memoria
        candidatos = aplicarFiltrosEstructurados(candidatos, filtros);

        // 3) Ordenamiento
        SortOption sortOption = parseSortOption(filtros.getSort());

        if (hayBusqueda) {
            // Si el usuario pidió orden por precio, respetarlo
            if (sortOption == SortOption.PRICE_ASC || sortOption == SortOption.PRICE_DESC) {
                List<Producto> ordenados = ordenarProductos(candidatos, sortOption);
                List<ProductoDto> productos = ordenados.stream()
                        .map(productoMapper::toResumenDto)
                        .toList();
                productos = aplicarRandomSiCorresponde(productos, filtros);
                return paginar(productos, filtros);
            }

            // Default: relevancia cuando hay búsqueda
            List<ProductoDto> productos = rankearPorRelevancia(candidatos, q.trim());
            productos = aplicarRandomSiCorresponde(productos, filtros);
            return paginar(productos, filtros);
        }

        // Sin búsqueda: ordenar (por defecto RECENT)
        List<Producto> ordenados = ordenarProductos(candidatos, sortOption);
        List<ProductoDto> productos = ordenados.stream()
                .map(productoMapper::toResumenDto)
                .toList();
        productos = aplicarRandomSiCorresponde(productos, filtros);
        return paginar(productos, filtros);
    }

    /* =========================================================================
       Detalle por id (CU-08)
       ========================================================================= */

    @Override
    @Transactional(readOnly = true)
    public ProductoDto obtenerPorId(Integer idProducto) {
        Producto producto = productoRepository.findByIdActivo(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", idProducto));

        // Forzamos la carga de las colecciones lazy dentro de la misma transacción
        producto.getVariantes().size();
        producto.getImagenes().size();
        producto.getEspecificaciones().size();
        producto.getDescuentosVolumen().size();
        producto.getCategorias().size();

        return productoMapper.toDetalleDto(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarPorTienda(Integer idTienda) {
        return productoRepository.findByTiendaId(idTienda).stream()
                .map(productoMapper::toResumenDto)
                .toList();
    }

    /* =========================================================================
       Helpers privados
       ========================================================================= */

    /**
     * Aplica los filtros estructurados (categorías, precio, tipo de servicio)
     * sobre la lista en memoria. Cuando el volumen lo justifique, esta lógica
     * se moverá a la BD vía Specifications API o Criteria.
     */
    private List<Producto> aplicarFiltrosEstructurados(List<Producto> productos, FiltrosCatalogoDto f) {
        return productos.stream()
                .filter(p -> categoriaCoincide(p, f.getCategorias()))
                .filter(p -> tipoServicioCoincide(p, f.getTipoServicio()))
                .filter(p -> precioEnRango(p.getPrecioBase(), f.getPrecioMin(), f.getPrecioMax()))
                .toList();
    }

    private boolean categoriaCoincide(Producto p, List<String> categorias) {
        if (categorias == null || categorias.isEmpty()) return true;
        return p.getCategorias().stream()
                .map(Categoria::getNombreCategoria)
                .anyMatch(categorias::contains);
    }

    private boolean tipoServicioCoincide(Producto p, String tipoServicio) {
        if (tipoServicio == null || tipoServicio.isBlank()) return true;
        // El frontend envía COMPRA_DIRECTA / PERSONALIZABLE / COTIZACION
        // En BD solo tenemos esPersonalizable. COTIZACION no aplica al producto.
        return switch (tipoServicio) {
            case "PERSONALIZABLE" -> Boolean.TRUE.equals(p.getEsPersonalizable());
            case "COMPRA_DIRECTA" -> !Boolean.TRUE.equals(p.getEsPersonalizable());
            default -> true;
        };
    }

    private boolean precioEnRango(Double precio, Double min, Double max) {
        if (precio == null) return min == null && max == null;
        if (min != null && precio < min) return false;
        if (max != null && precio > max) return false;
        return true;
    }

    /**
     * Calcula score de relevancia para cada producto y los ordena descendente.
     *
     * Algoritmo idéntico al mock del frontend (mantiene comportamiento al cambiar
     * de mocks a backend):
     *   - Por cada palabra de q (split por espacios):
     *     - +10 si está en titulo
     *     - +5  si está en descripcion
     *     - +5  si está en nombreTienda
     *     - +3  si está en categoria
     */
    private List<ProductoDto> rankearPorRelevancia(List<Producto> productos, String q) {
        String queryNorm = normalizar(q);
        String[] palabras = queryNorm.split("\\s+");

        // LinkedHashMap para mantener orden de inserción al sortear
        Map<Producto, Integer> scoresMap = new LinkedHashMap<>();
        for (Producto p : productos) {
            int score = calcularScore(p, palabras);
            if (score > 0) scoresMap.put(p, score);
        }

        return scoresMap.entrySet().stream()
                .sorted(Map.Entry.<Producto, Integer>comparingByValue().reversed())
                .map(e -> productoMapper.toResumenDto(e.getKey()))
                .toList();
    }

    private int calcularScore(Producto p, String[] palabras) {
        String titulo      = normalizar(p.getNombre());
        String descripcion = normalizar(p.getDescripcion() != null ? p.getDescripcion() : "");
        String tienda      = normalizar(
                p.getTienda() != null && p.getTienda().getNombreComercial() != null
                        ? p.getTienda().getNombreComercial() : "");
        String categorias  = normalizar(p.getCategorias().stream()
                .map(Categoria::getNombreCategoria)
                .reduce("", (acc, c) -> acc + " " + c));

        int score = 0;
        for (String palabra : palabras) {
            if (palabra.isEmpty()) continue;
            if (titulo.contains(palabra))      score += PESO_TITULO;
            if (descripcion.contains(palabra)) score += PESO_DESCRIPCION;
            if (tienda.contains(palabra))      score += PESO_TIENDA;
            if (categorias.contains(palabra))  score += PESO_CATEGORIA;
        }
        return score;
    }

    /**
     * Normaliza texto: lowercase + quita acentos.
     * "Algodón" → "algodon" → matchea con búsqueda "algodon".
     */
    private String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase();
    }

    private enum SortOption {
        RECENT,
        PRICE_ASC,
        PRICE_DESC,
        RELEVANCIA
    }

    private SortOption parseSortOption(String sortRaw) {
        if (sortRaw == null || sortRaw.isBlank()) return SortOption.RECENT;
        String key = normalizar(sortRaw).replace(' ', '_');

        return switch (key) {
            case "menor_precio", "precio_asc", "price_asc" -> SortOption.PRICE_ASC;
            case "mayor_precio", "precio_desc", "price_desc" -> SortOption.PRICE_DESC;
            case "relevancia" -> SortOption.RELEVANCIA;
            case "lo_mas_reciente", "reciente", "recent" -> SortOption.RECENT;
            default -> SortOption.RECENT;
        };
    }

    private List<Producto> ordenarProductos(List<Producto> productos, SortOption sortOption) {
        Comparator<Producto> comparator = switch (sortOption) {
            case PRICE_ASC -> Comparator.comparing(p -> precioOrdenable(p.getPrecioBase()));
            case PRICE_DESC -> Comparator.comparing((Producto p) -> precioOrdenable(p.getPrecioBase())).reversed();
            case RELEVANCIA, RECENT -> Comparator.comparing(Producto::getIdProducto).reversed();
        };

        return productos.stream()
                .sorted(comparator)
                .toList();
    }

    private double precioOrdenable(Double precioBase) {
        return precioBase == null ? Double.MAX_VALUE : precioBase;
    }

    private PagedResponse<ProductoDto> paginar(List<ProductoDto> items, FiltrosCatalogoDto filtros) {
        int page = filtros.getPage() == null ? 1 : filtros.getPage();
        int size = filtros.getSize() == null ? 12 : filtros.getSize();
        if (page < 1) page = 1;
        if (size < 1) size = 12;

        int totalItems = items.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int fromIndex = Math.min((page - 1) * size, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);

        List<ProductoDto> pageItems = items.subList(fromIndex, toIndex);

        return PagedResponse.<ProductoDto>builder()
                .items(pageItems)
                .page(page)
                .size(size)
                .totalItems((long) totalItems)
                .totalPages(totalPages)
                .build();
    }

    private List<ProductoDto> aplicarRandomSiCorresponde(
            List<ProductoDto> productos,
            FiltrosCatalogoDto filtros) {
        if (filtros.getRandom() == null || !filtros.getRandom()) return productos;
        long seed = filtros.getSeed() != null ? filtros.getSeed() : System.currentTimeMillis();
        List<ProductoDto> copia = new java.util.ArrayList<>(productos);
        Collections.shuffle(copia, new Random(seed));
        return copia;
    }
}
