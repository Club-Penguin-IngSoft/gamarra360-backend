package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.StockResponse;
import pe.com.gamarra360.backend.catalogo.dto.StockUpdateRequest;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.service.CrudService;

public interface VarianteProductoService extends CrudService<VarianteProducto, Integer> {

    StockResponse actualizarStock(Integer idVariante, StockUpdateRequest request);

    StockResponse consultarStock(Integer idVariante);

    void descontarStock(Integer idVariante, Integer cantidad);
}