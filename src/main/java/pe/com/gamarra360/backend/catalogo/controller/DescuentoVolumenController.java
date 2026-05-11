package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.DescuentoVolumen;
import pe.com.gamarra360.backend.catalogo.service.DescuentoVolumenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/descuentos-volumen")
@Slf4j
public class DescuentoVolumenController {
    private final DescuentoVolumenService service;

    public DescuentoVolumenController(DescuentoVolumenService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DescuentoVolumen>> listar() {
        log.info("GET /api/v1/descuentos-volumen");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DescuentoVolumen> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/descuentos-volumen/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<DescuentoVolumen> crear(@RequestBody DescuentoVolumen request) {
        log.info("POST /api/v1/descuentos-volumen");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DescuentoVolumen> actualizar(@PathVariable Integer id, @RequestBody DescuentoVolumen request) {
        log.info("PUT /api/v1/descuentos-volumen/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/descuentos-volumen/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
