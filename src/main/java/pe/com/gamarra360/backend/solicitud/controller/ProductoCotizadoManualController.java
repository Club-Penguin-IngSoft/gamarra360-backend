package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.ProductoCotizadoManual;
import pe.com.gamarra360.backend.solicitud.service.ProductoCotizadoManualService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos-cotizados-manual")
@Slf4j
public class ProductoCotizadoManualController {
    private final ProductoCotizadoManualService service;

    public ProductoCotizadoManualController(ProductoCotizadoManualService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProductoCotizadoManual>> listar() {
        log.info("GET /api/v1/productos-cotizados-manual");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoCotizadoManual> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/productos-cotizados-manual/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ProductoCotizadoManual> crear(@RequestBody ProductoCotizadoManual request) {
        log.info("POST /api/v1/productos-cotizados-manual");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoCotizadoManual> actualizar(@PathVariable Long id, @RequestBody ProductoCotizadoManual request) {
        log.info("PUT /api/v1/productos-cotizados-manual/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/productos-cotizados-manual/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
