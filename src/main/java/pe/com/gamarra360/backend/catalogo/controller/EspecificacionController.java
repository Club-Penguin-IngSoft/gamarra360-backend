package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.Especificacion;
import pe.com.gamarra360.backend.catalogo.service.EspecificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especificaciones")
@Slf4j
public class EspecificacionController {
    private final EspecificacionService service;

    public EspecificacionController(EspecificacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Especificacion>> listar() {
        log.info("GET /api/v1/especificaciones");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Especificacion> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/especificaciones/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Especificacion> crear(@RequestBody Especificacion request) {
        log.info("POST /api/v1/especificaciones");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Especificacion> actualizar(@PathVariable Integer id, @RequestBody Especificacion request) {
        log.info("PUT /api/v1/especificaciones/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/especificaciones/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
