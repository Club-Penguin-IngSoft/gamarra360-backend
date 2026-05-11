package pe.com.gamarra360.backend.usuario.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.service.ComercianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comerciantes")
@Slf4j
public class ComercianteController {
    private final ComercianteService service;

    public ComercianteController(ComercianteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Comerciante>> listar() {
        log.info("GET /api/v1/comerciantes");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comerciante> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/comerciantes/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Comerciante> crear(@RequestBody Comerciante request) {
        log.info("POST /api/v1/comerciantes");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comerciante> actualizar(@PathVariable Integer id, @RequestBody Comerciante request) {
        log.info("PUT /api/v1/comerciantes/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/comerciantes/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
