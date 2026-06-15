package pe.com.gamarra360.backend.catalogo.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.catalogo.dto.TipoProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.TipoProductoResponse;
import pe.com.gamarra360.backend.catalogo.service.TipoProductoService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tipos-producto")
public class TipoProductoController {

    private final TipoProductoService service;

    public TipoProductoController(TipoProductoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TipoProductoResponse>> listar(
            @RequestParam(required = false) Integer categoriaId) {
        log.info("GET /api/v1/tipos-producto categoriaId={}", categoriaId);
        if (categoriaId != null) {
            return ResponseEntity.ok(service.listarPorCategoria(categoriaId));
        }
        return ResponseEntity.ok(service.listarTodosComoResponse());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoProductoResponse> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/tipos-producto/{}", id);
        return ResponseEntity.ok(service.obtenerComoResponse(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TipoProductoResponse> crear(
            @Valid @RequestBody TipoProductoRequest request) {
        log.info("POST /api/v1/tipos-producto");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearTipoProducto(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TipoProductoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody TipoProductoRequest request) {
        log.info("PUT /api/v1/tipos-producto/{}", id);
        return ResponseEntity.ok(service.actualizarTipoProducto(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/tipos-producto/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}