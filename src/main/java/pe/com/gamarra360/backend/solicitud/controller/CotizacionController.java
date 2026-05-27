package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;
import pe.com.gamarra360.backend.solicitud.service.CotizacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cotizaciones")
@Slf4j
public class CotizacionController {
    private final CotizacionService service;

    public CotizacionController(CotizacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cotizacion>> listar() {
        log.info("GET /api/v1/cotizaciones");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cotizacion> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/cotizaciones/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Cotizacion> crear(@RequestBody Cotizacion request) {
        log.info("POST /api/v1/cotizaciones");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cotizacion> actualizar(@PathVariable Long id, @RequestBody Cotizacion request) {
        log.info("PUT /api/v1/cotizaciones/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/cotizaciones/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
