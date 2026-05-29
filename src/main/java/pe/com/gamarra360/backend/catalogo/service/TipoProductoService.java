package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.TipoProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.TipoProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;
import pe.com.gamarra360.backend.service.CrudService;

import java.util.List;

public interface TipoProductoService extends CrudService<TipoProducto, Integer> {
    List<TipoProductoResponse> listarTodosComoResponse();
    List<TipoProductoResponse> listarPorCategoria(Integer idCategoria);
    TipoProductoResponse obtenerComoResponse(Integer id);
    TipoProductoResponse crearTipoProducto(TipoProductoRequest request);
    TipoProductoResponse actualizarTipoProducto(Integer id, TipoProductoRequest request);
}