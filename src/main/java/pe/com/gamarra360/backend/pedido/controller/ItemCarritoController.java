package pe.com.gamarra360.backend.pedido.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.pedido.entity.ItemCarrito;
import pe.com.gamarra360.backend.pedido.service.ItemCarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items-carrito")
@Slf4j
public class ItemCarritoController {
    private final ItemCarritoService service;

    public ItemCarritoController(ItemCarritoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ItemCarrito>> listar() {
        log.info("GET /api/v1/items-carrito");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemCarrito> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/items-carrito/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ItemCarrito> crear(@RequestBody ItemCarrito request) {
        log.info("POST /api/v1/items-carrito");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCarrito> actualizar(@PathVariable Long id, @RequestBody ItemCarrito request) {
        log.info("PUT /api/v1/items-carrito/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/items-carrito/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
