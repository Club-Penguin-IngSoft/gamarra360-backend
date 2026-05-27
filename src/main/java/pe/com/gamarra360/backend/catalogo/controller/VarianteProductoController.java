package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.service.VarianteProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variantes-producto")
@Slf4j
public class VarianteProductoController {
    private final VarianteProductoService service;

    public VarianteProductoController(VarianteProductoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VarianteProducto>> listar() {
        log.info("GET /api/v1/variantes-producto");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteProducto> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/variantes-producto/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<VarianteProducto> crear(@RequestBody VarianteProducto request) {
        log.info("POST /api/v1/variantes-producto");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VarianteProducto> actualizar(@PathVariable Integer id, @RequestBody VarianteProducto request) {
        log.info("PUT /api/v1/variantes-producto/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/variantes-producto/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
