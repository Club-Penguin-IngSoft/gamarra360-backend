package com.gamarra360.catalogo.controller;

import com.gamarra360.catalogo.dto.FiltrosCatalogoDto;
import com.gamarra360.catalogo.dto.PagedResponse;
import com.gamarra360.catalogo.dto.ProductoDto;
import com.gamarra360.catalogo.service.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST del módulo `catalogo`.
 *
 * Endpoints implementados (CU-07 + CU-08):
 *  - GET /api/v1/productos          → listar catálogo con filtros + búsqueda
 *  - GET /api/v1/productos/{id}     → detalle completo del producto
 *  - GET /api/v1/productos/tienda/{idTienda} → productos de una tienda
 *
 * Sigue las convenciones CLAUDE.md §5:
 *  - Rutas en /api/v1/{recurso} en plural y kebab-case
 *  - Sin lógica de negocio en el controller (delega 100% al service)
 *  - JSON in, JSON out
 *  - No requiere autenticación (catálogo público)
 */
@RestController
@RequestMapping("/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Lista productos del catálogo público.
     *
     * Query params (todos opcionales):
     *   - q                 → palabra clave para búsqueda por relevancia
     *   - categorias        → repeatable (?categorias=HOMBRE&categorias=MUJER)
     *   - tipoServicio      → "COMPRA_DIRECTA" | "PERSONALIZABLE"
     *   - precioMin, precioMax
     *   - color, material, tallas, entrega, tiposProducto (multi)
     *
     * Ejemplo:
     *   GET /api/v1/productos?q=polo&categorias=HOMBRE&precioMin=20
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ProductoDto>> listar(FiltrosCatalogoDto filtros) {
        log.debug("GET /productos con filtros: {}", filtros);
        PagedResponse<ProductoDto> productos = productoService.listarConFiltros(filtros);
        return ResponseEntity.ok(productos);
    }

    /**
     * Devuelve la ficha completa de un producto (CU-08).
     * Incluye variantes con stock, imágenes ordenadas, especificaciones
     * y reglas de descuento.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> obtener(@PathVariable Integer id) {
        log.debug("GET /productos/{}", id);
        ProductoDto producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    /**
     * Lista productos de una tienda específica. Usado en el perfil de tienda.
     *
     * Ejemplo:
     *   GET /api/v1/productos/tienda/3
     */
    @GetMapping("/tienda/{idTienda}")
    public ResponseEntity<List<ProductoDto>> listarPorTienda(@PathVariable Integer idTienda) {
        log.debug("GET /productos/tienda/{}", idTienda);
        List<ProductoDto> productos = productoService.listarPorTienda(idTienda);
        return ResponseEntity.ok(productos);
    }
}
