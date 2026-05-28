package pe.com.gamarra360.backend.catalogo.service;

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
}
