package pe.com.gamarra360.backend.catalogo.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import pe.com.gamarra360.backend.catalogo.dto.OpcionesFiltroDto;
import pe.com.gamarra360.backend.catalogo.dto.PaginaResponse;
import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.service.ProductoService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;

import java.util.List;

/**
 * Controller REST del módulo `catalogo`.
 *
 * Endpoints (CU-07 + CU-08 + RF-15/16/17):
 *  - GET    /api/v1/productos                    → catálogo público
 *  - GET    /api/v1/productos/{id}               → detalle de producto
 *  - GET    /api/v1/productos/tienda/{idTienda}  → productos de una tienda
 *  - POST   /api/v1/productos                    → crear producto (VENDEDOR)
 *  - PUT    /api/v1/productos/{id}               → editar producto (VENDEDOR)
 *  - DELETE /api/v1/productos/{id}               → eliminar producto (VENDEDOR)
 *
 * Sigue las convenciones CLAUDE.md §5.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    /**
     * Lista productos activos con paginación server-side (catálogo público).
     *
     * @param page página 0-based (default 0)
     * @param size elementos por página (default 12, máx recomendado 500)
     */
    @GetMapping
    public ResponseEntity<PaginaResponse<ProductoResponse>> listar(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size) {
        log.info("GET /api/v1/productos?page={}&size={}", page, size);
        return ResponseEntity.ok(service.listarPaginado(page, size));
    }

    /** Obtiene un producto por ID con detalle completo (público). */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/productos/{}", id);
        return ResponseEntity.ok(service.obtenerProductoResponse(id));
    }

    /**
     * Devuelve los N productos más recientes por cada categoría activa.
     * Diseñado para la sección "Catálogo" del inicio — una sola llamada eficiente.
     * Clave del mapa = nombre de categoría (ej. "Hombre").
     */
    @GetMapping("/destacados")
    public ResponseEntity<java.util.Map<String, List<ProductoResponse>>> destacados(
            @RequestParam(defaultValue = "8") int porCategoria) {
        log.info("GET /api/v1/productos/destacados?porCategoria={}", porCategoria);
        return ResponseEntity.ok(service.listarDestacados(porCategoria));
    }

    /**
     * Búsqueda de productos por keyword en nombre, descripción y nombre de tienda.
     * Solo devuelve productos de catálogo público (tienda y comerciante verificados).
     * Usado por el buscador del TopBar.
     *
     * @param q    término de búsqueda
     * @param size máximo de resultados (default 6)
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "6") int size) {
        log.info("GET /api/v1/productos/buscar?q={}&size={}", q, size);
        return ResponseEntity.ok(service.buscarPorKeyword(q, size));
    }

    /**
     * Devuelve las opciones disponibles para los filtros del catálogo
     * (colores, materiales, tallas, tipos de producto) desde la BD real.
     * Endpoint público — no requiere autenticación.
     */
    @GetMapping("/opciones-filtro")
    public ResponseEntity<OpcionesFiltroDto> opcionesFiltro() {
        log.info("GET /api/v1/productos/opciones-filtro");
        return ResponseEntity.ok(service.obtenerOpcionesFiltro());
    }

    /** Lista los productos activos de una tienda específica (RF-15). */
    @GetMapping("/tienda/{idTienda}")
    public ResponseEntity<List<ProductoResponse>> listarPorTienda(@PathVariable Integer idTienda) {
        log.info("GET /api/v1/productos/tienda/{}", idTienda);
        return ResponseEntity.ok(service.listarPorTienda(idTienda));
    }

    /**
     * Crea un producto en la tienda del comerciante autenticado (RF-15).
     * Valida que el comerciante esté verificado y que la tienda le pertenezca.
     */
    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request,
            Authentication auth) {
        log.info("POST /api/v1/productos");
        Integer comercianteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearProducto(request, comercianteId));
    }

    /**
     * Edita un producto validando que pertenezca a la tienda del comerciante (RF-16).
     * Retorna 403 si intenta editar un producto de otra tienda.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequest request,
            Authentication auth) {
        log.info("PUT /api/v1/productos/{}", id);
        Integer comercianteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        return ResponseEntity.ok(service.actualizarProducto(id, request, comercianteId));
    }

    /**
     * Eliminación lógica (activo=false). Retorna 409 si hay pedidos activos
     * o cotizaciones en curso; 403 si es producto de otra tienda (RF-17).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id,
            Authentication auth) {
        log.info("DELETE /api/v1/productos/{}", id);
        Integer comercianteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        service.eliminarProducto(id, comercianteId);
        return ResponseEntity.noContent().build();
    }
}
