package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.Color;
import pe.com.gamarra360.backend.catalogo.service.ColorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/colores")
@Slf4j
public class ColorController {
    private final ColorService service;

    public ColorController(ColorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Color>> listar() {
        log.info("GET /api/v1/colores");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Color> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/colores/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Color> crear(@RequestBody Color request) {
        log.info("POST /api/v1/colores");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Color> actualizar(@PathVariable Integer id, @RequestBody Color request) {
        log.info("PUT /api/v1/colores/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/colores/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
