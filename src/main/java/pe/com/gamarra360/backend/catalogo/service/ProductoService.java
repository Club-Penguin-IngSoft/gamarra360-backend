package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.service.CrudService;

import java.util.List;

public interface ProductoService extends CrudService<Producto, Integer> {

    List<ProductoResponse> listarTodosComoResponse();

    List<ProductoResponse> listarPorTienda(Integer idTienda);

    ProductoResponse obtenerProductoResponse(Integer idProducto);

    ProductoResponse crearProducto(ProductoRequest request, Integer comercianteId);

    ProductoResponse actualizarProducto(Integer idProducto, ProductoRequest request, Integer comercianteId);

    void eliminarProducto(Integer idProducto, Integer comercianteId);
}