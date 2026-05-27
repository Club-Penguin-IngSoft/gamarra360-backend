package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.RespuestaSolicitud;
import pe.com.gamarra360.backend.solicitud.service.RespuestaSolicitudService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/respuestas-solicitud")
@Slf4j
public class RespuestaSolicitudController {
    private final RespuestaSolicitudService service;

    public RespuestaSolicitudController(RespuestaSolicitudService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RespuestaSolicitud>> listar() {
        log.info("GET /api/v1/respuestas-solicitud");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaSolicitud> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/respuestas-solicitud/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<RespuestaSolicitud> crear(@RequestBody RespuestaSolicitud request) {
        log.info("POST /api/v1/respuestas-solicitud");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaSolicitud> actualizar(@PathVariable Long id, @RequestBody RespuestaSolicitud request) {
        log.info("PUT /api/v1/respuestas-solicitud/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/respuestas-solicitud/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
