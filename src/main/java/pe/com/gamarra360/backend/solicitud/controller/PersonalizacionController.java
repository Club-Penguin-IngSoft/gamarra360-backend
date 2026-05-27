package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import pe.com.gamarra360.backend.solicitud.service.PersonalizacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personalizaciones")
@Slf4j
public class PersonalizacionController {
    private final PersonalizacionService service;

    public PersonalizacionController(PersonalizacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Personalizacion>> listar() {
        log.info("GET /api/v1/personalizaciones");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personalizacion> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/personalizaciones/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Personalizacion> crear(@RequestBody Personalizacion request) {
        log.info("POST /api/v1/personalizaciones");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Personalizacion> actualizar(@PathVariable Long id, @RequestBody Personalizacion request) {
        log.info("PUT /api/v1/personalizaciones/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/personalizaciones/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
