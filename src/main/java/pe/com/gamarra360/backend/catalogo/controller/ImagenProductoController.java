package pe.com.gamarra360.backend.catalogo.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.ImagenProducto;
import pe.com.gamarra360.backend.catalogo.service.ImagenProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/imagenes-producto")
@Slf4j
public class ImagenProductoController {
    private final ImagenProductoService service;

    public ImagenProductoController(ImagenProductoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ImagenProducto>> listar() {
        log.info("GET /api/v1/imagenes-producto");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenProducto> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/imagenes-producto/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ImagenProducto> crear(@RequestBody ImagenProducto request) {
        log.info("POST /api/v1/imagenes-producto");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImagenProducto> actualizar(@PathVariable Integer id, @RequestBody ImagenProducto request) {
        log.info("PUT /api/v1/imagenes-producto/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/imagenes-producto/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
