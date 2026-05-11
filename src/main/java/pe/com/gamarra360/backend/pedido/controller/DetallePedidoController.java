package pe.com.gamarra360.backend.pedido.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.service.DetallePedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/detalles-pedido")
@Slf4j
public class DetallePedidoController {
    private final DetallePedidoService service;

    public DetallePedidoController(DetallePedidoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DetallePedido>> listar() {
        log.info("GET /api/v1/detalles-pedido");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedido> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/detalles-pedido/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<DetallePedido> crear(@RequestBody DetallePedido request) {
        log.info("POST /api/v1/detalles-pedido");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallePedido> actualizar(@PathVariable Long id, @RequestBody DetallePedido request) {
        log.info("PUT /api/v1/detalles-pedido/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/detalles-pedido/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
