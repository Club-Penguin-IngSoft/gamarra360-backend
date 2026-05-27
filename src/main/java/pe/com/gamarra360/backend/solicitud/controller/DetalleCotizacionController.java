package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.DetalleCotizacion;
import pe.com.gamarra360.backend.solicitud.service.DetalleCotizacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/detalles-cotizacion")
@Slf4j
public class DetalleCotizacionController {
    private final DetalleCotizacionService service;

    public DetalleCotizacionController(DetalleCotizacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DetalleCotizacion>> listar() {
        log.info("GET /api/v1/detalles-cotizacion");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleCotizacion> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/detalles-cotizacion/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<DetalleCotizacion> crear(@RequestBody DetalleCotizacion request) {
        log.info("POST /api/v1/detalles-cotizacion");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalleCotizacion> actualizar(@PathVariable Integer id, @RequestBody DetalleCotizacion request) {
        log.info("PUT /api/v1/detalles-cotizacion/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/detalles-cotizacion/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
