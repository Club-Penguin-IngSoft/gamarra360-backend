package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.DetallePersonalizacion;
import pe.com.gamarra360.backend.solicitud.service.DetallePersonalizacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/detalles-personalizacion")
@Slf4j
public class DetallePersonalizacionController {
    private final DetallePersonalizacionService service;

    public DetallePersonalizacionController(DetallePersonalizacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DetallePersonalizacion>> listar() {
        log.info("GET /api/v1/detalles-personalizacion");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePersonalizacion> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/detalles-personalizacion/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<DetallePersonalizacion> crear(@RequestBody DetallePersonalizacion request) {
        log.info("POST /api/v1/detalles-personalizacion");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallePersonalizacion> actualizar(@PathVariable Integer id, @RequestBody DetallePersonalizacion request) {
        log.info("PUT /api/v1/detalles-personalizacion/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/detalles-personalizacion/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
