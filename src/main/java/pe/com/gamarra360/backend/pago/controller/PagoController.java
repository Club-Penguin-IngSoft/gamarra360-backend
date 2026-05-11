package pe.com.gamarra360.backend.pago.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.pago.entity.Pago;
import pe.com.gamarra360.backend.pago.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@Slf4j
public class PagoController {
    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Pago>> listar() {
        log.info("GET /api/v1/pagos");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/pagos/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Pago> crear(@RequestBody Pago request) {
        log.info("POST /api/v1/pagos");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pago> actualizar(@PathVariable Long id, @RequestBody Pago request) {
        log.info("PUT /api/v1/pagos/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/pagos/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
