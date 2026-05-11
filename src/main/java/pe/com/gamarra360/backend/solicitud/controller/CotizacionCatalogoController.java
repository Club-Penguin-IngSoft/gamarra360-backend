package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.CotizacionCatalogo;
import pe.com.gamarra360.backend.solicitud.service.CotizacionCatalogoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cotizaciones-catalogo")
@Slf4j
public class CotizacionCatalogoController {
    private final CotizacionCatalogoService service;

    public CotizacionCatalogoController(CotizacionCatalogoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CotizacionCatalogo>> listar() {
        log.info("GET /api/v1/cotizaciones-catalogo");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CotizacionCatalogo> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/cotizaciones-catalogo/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<CotizacionCatalogo> crear(@RequestBody CotizacionCatalogo request) {
        log.info("POST /api/v1/cotizaciones-catalogo");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CotizacionCatalogo> actualizar(@PathVariable Integer id, @RequestBody CotizacionCatalogo request) {
        log.info("PUT /api/v1/cotizaciones-catalogo/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/cotizaciones-catalogo/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
