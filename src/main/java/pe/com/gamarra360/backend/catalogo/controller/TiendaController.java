package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.service.TiendaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tiendas")
@Slf4j
public class TiendaController {
    private final TiendaService service;

    public TiendaController(TiendaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Tienda>> listar() {
        log.info("GET /api/v1/tiendas");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tienda> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/tiendas/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Tienda> crear(@RequestBody Tienda request) {
        log.info("POST /api/v1/tiendas");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tienda> actualizar(@PathVariable Integer id, @RequestBody Tienda request) {
        log.info("PUT /api/v1/tiendas/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/tiendas/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
