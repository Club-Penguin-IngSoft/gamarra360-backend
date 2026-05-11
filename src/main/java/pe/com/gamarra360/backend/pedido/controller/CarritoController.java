package pe.com.gamarra360.backend.pedido.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.pedido.entity.Carrito;
import pe.com.gamarra360.backend.pedido.service.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carritos")
@Slf4j
public class CarritoController {
    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Carrito>> listar() {
        log.info("GET /api/v1/carritos");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrito> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/carritos/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Carrito> crear(@RequestBody Carrito request) {
        log.info("POST /api/v1/carritos");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carrito> actualizar(@PathVariable Long id, @RequestBody Carrito request) {
        log.info("PUT /api/v1/carritos/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/carritos/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
