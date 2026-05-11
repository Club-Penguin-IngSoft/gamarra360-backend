package pe.com.gamarra360.backend.solicitud.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.solicitud.entity.ItemPersonalizado;
import pe.com.gamarra360.backend.solicitud.service.ItemPersonalizadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items-personalizados")
@Slf4j
public class ItemPersonalizadoController {
    private final ItemPersonalizadoService service;

    public ItemPersonalizadoController(ItemPersonalizadoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ItemPersonalizado>> listar() {
        log.info("GET /api/v1/items-personalizados");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemPersonalizado> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/items-personalizados/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ItemPersonalizado> crear(@RequestBody ItemPersonalizado request) {
        log.info("POST /api/v1/items-personalizados");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemPersonalizado> actualizar(@PathVariable Long id, @RequestBody ItemPersonalizado request) {
        log.info("PUT /api/v1/items-personalizados/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/items-personalizados/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
