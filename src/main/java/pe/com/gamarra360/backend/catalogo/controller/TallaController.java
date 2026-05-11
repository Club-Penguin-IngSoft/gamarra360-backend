package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.Talla;
import pe.com.gamarra360.backend.catalogo.service.TallaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tallas")
@Slf4j
public class TallaController {
    private final TallaService service;

    public TallaController(TallaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Talla>> listar() {
        log.info("GET /api/v1/tallas");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Talla> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/tallas/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Talla> crear(@RequestBody Talla request) {
        log.info("POST /api/v1/tallas");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Talla> actualizar(@PathVariable Integer id, @RequestBody Talla request) {
        log.info("PUT /api/v1/tallas/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/tallas/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
