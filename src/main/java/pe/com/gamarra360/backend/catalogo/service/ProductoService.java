package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.OpcionesFiltroDto;
import pe.com.gamarra360.backend.catalogo.dto.PaginaResponse;
import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.service.CrudService;

import java.util.List;

/**
 * Contrato del servicio de productos.
 *
 * Extiende CrudService para las operaciones CRUD base (RF-15, RF-16, RF-17).
 * Agrega métodos específicos del catálogo de Gamarra 360°.
 */
public interface ProductoService extends CrudService<Producto, Integer> {

    /** Lista todos los productos activos como DTO de respuesta (catálogo público). */
    List<ProductoResponse> listarTodosComoResponse();

    /**
     * Lista productos activos de forma paginada (server-side pagination).
     *
     * @param page índice 0-based de la página
     * @param size número de elementos por página
     */
    PaginaResponse<ProductoResponse> listarPaginado(int page, int size);

    /** Lista los productos activos de una tienda específica. */
    List<ProductoResponse> listarPorTienda(Integer idTienda);

    /** Devuelve el detalle completo de un producto por ID. */
    ProductoResponse obtenerProductoResponse(Integer idProducto);

    /** Crea un producto validando que el comerciante esté verificado y sea dueño de la tienda (RF-15). */
    ProductoResponse crearProducto(ProductoRequest request, Integer comercianteId);

    /** Edita un producto validando que pertenezca a la tienda del comerciante (RF-16). */
    ProductoResponse actualizarProducto(Integer idProducto, ProductoRequest request, Integer comercianteId);

    /** Eliminación lógica (activo=false) con validación de pedidos/cotizaciones activas (RF-17). */
    void eliminarProducto(Integer idProducto, Integer comercianteId);

    /**
     * Devuelve los valores disponibles para los filtros del catálogo (colores, materiales,
     * tallas y tipos de producto) derivados directamente de la BD.
     */
    OpcionesFiltroDto obtenerOpcionesFiltro();

    /**
     * Devuelve los `porCategoria` productos más recientes por cada categoría activa.
     * Diseñado para la sección "Catálogo" del inicio — una sola query eficiente.
     * La clave del mapa es el nombre de la categoría (ej. "Hombre", "Mujer").
     */
    java.util.Map<String, List<ProductoResponse>> listarDestacados(int porCategoria);
}
