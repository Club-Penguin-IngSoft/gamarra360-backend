package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.FiltrosCatalogoDto;
import pe.com.gamarra360.backend.catalogo.dto.PagedResponse;
import pe.com.gamarra360.backend.catalogo.dto.ProductoDto;

import java.util.List;

/**
 * Contrato del servicio de productos (CU-07 + CU-08).
 *
 * Es la única superficie pública del módulo `catalogo`. Otros módulos
 * (carrito, pedido, etc.) deben consumir este servicio — NUNCA acceder
 * directamente al repositorio (CLAUDE.md §4).
 */
public interface ProductoService {

    /**
     * Lista productos del catálogo público aplicando filtros y, si hay `q`,
     * ranqueando por relevancia (CU-08, RF-22/RF-23).
     *
     * @param filtros estructura con todos los filtros (search, categorías, precio, etc.)
     * @return lista de productos (formato resumido, sin variantes/specs/descuentos)
     */
    PagedResponse<ProductoDto> listarConFiltros(FiltrosCatalogoDto filtros);

    /**
     * Devuelve la ficha completa de un producto por id, incluyendo variantes,
     * imágenes, especificaciones y reglas de descuento (CU-08).
     *
     * @param idProducto id del producto
     * @return DTO completo
    * @throws pe.com.gamarra360.backend.exception.RecursoNoEncontradoException si no existe o está inactivo
     */
    ProductoDto obtenerPorId(Integer idProducto);

    /**
     * Lista productos de una tienda específica.
     * Útil para el perfil de tienda en el frontend.
     */
    List<ProductoDto> listarPorTienda(Integer idTienda);
}
