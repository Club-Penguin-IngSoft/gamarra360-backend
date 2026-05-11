package pe.com.gamarra360.backend.pago.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.service.OrdenPagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes-pago")
@Slf4j
public class OrdenPagoController {
    private final OrdenPagoService service;

    public OrdenPagoController(OrdenPagoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OrdenPago>> listar() {
        log.info("GET /api/v1/ordenes-pago");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenPago> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/ordenes-pago/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<OrdenPago> crear(@RequestBody OrdenPago request) {
        log.info("POST /api/v1/ordenes-pago");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenPago> actualizar(@PathVariable Long id, @RequestBody OrdenPago request) {
        log.info("PUT /api/v1/ordenes-pago/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/ordenes-pago/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
