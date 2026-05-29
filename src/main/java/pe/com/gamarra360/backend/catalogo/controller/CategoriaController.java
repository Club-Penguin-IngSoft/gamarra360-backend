package pe.com.gamarra360.backend.catalogo.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.catalogo.dto.CategoriaRequest;
import pe.com.gamarra360.backend.catalogo.dto.CategoriaResponse;
import pe.com.gamarra360.backend.catalogo.service.CategoriaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        log.info("GET /api/v1/categorias");
        return ResponseEntity.ok(service.listarTodosComoResponse());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/categorias/{}", id);
        return ResponseEntity.ok(service.obtenerComoResponse(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> crear(
            @Valid @RequestBody CategoriaRequest request) {
        log.info("POST /api/v1/categorias");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearCategoria(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequest request) {
        log.info("PUT /api/v1/categorias/{}", id);
        return ResponseEntity.ok(service.actualizarCategoria(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/categorias/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}