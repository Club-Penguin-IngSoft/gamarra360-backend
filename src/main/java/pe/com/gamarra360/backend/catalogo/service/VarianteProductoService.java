package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.StockResponse;
import pe.com.gamarra360.backend.catalogo.dto.StockUpdateRequest;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.service.CrudService;

public interface VarianteProductoService extends CrudService<VarianteProducto, Integer> {

    StockResponse actualizarStock(Integer idVariante, StockUpdateRequest request);

    StockResponse consultarStock(Integer idVariante);

    void descontarStock(Integer idVariante, Integer cantidad);

    void actualizarImagen(Integer idVariante, String imagenUrl);

    /**
     * Borrado físico de la variante con verificación de multi-tenancy.
     * Lanza RecursoNoEncontradoException (404) si no existe,
     * AccessDeniedException (403) si pertenece a otra tienda, y propaga
     * DataIntegrityViolationException (→ 409) si hay pedidos o ítems de
     * carrito que referencian la variante por FK.
     */
    void eliminarVariante(Integer idVariante, Integer comercianteId);
}