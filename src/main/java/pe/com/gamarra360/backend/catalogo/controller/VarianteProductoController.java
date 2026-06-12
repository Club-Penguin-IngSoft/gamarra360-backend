package pe.com.gamarra360.backend.catalogo.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pe.com.gamarra360.backend.catalogo.dto.StockResponse;
import pe.com.gamarra360.backend.catalogo.dto.StockUpdateRequest;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;

import java.util.Map;
import pe.com.gamarra360.backend.catalogo.service.VarianteProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variantes-producto")
@Slf4j
public class VarianteProductoController {

    private final VarianteProductoService service;

    public VarianteProductoController(VarianteProductoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VarianteProducto>> listar() {
        log.info("GET /api/v1/variantes-producto");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteProducto> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/variantes-producto/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<VarianteProducto> crear(@RequestBody VarianteProducto request) {
        log.info("POST /api/v1/variantes-producto");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<VarianteProducto> actualizar(
            @PathVariable Integer id,
            @RequestBody VarianteProducto request) {
        log.info("PUT /api/v1/variantes-producto/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/variantes-producto/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/imagen")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Void> actualizarImagen(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        log.info("PATCH /api/v1/variantes-producto/{}/imagen", id);
        service.actualizarImagen(id, body.get("imagenUrl"));
        return ResponseEntity.noContent().build();
    }

    /** Consulta el stock de una variante (RF-58). */
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponse> consultarStock(@PathVariable Integer id) {
        log.info("GET /api/v1/variantes-producto/{}/stock", id);
        return ResponseEntity.ok(service.consultarStock(id));
    }

    /**
     * Actualiza el stock manualmente. Si llega a 0, disponible pasa a false (RF-58).
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<StockResponse> actualizarStock(
            @PathVariable Integer id,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("PATCH /api/v1/variantes-producto/{}/stock", id);
        return ResponseEntity.ok(service.actualizarStock(id, request));
    }
}
