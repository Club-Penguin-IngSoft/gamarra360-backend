package pe.com.gamarra360.backend.usuario.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.usuario.entity.Admin;
import pe.com.gamarra360.backend.usuario.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
@Slf4j
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Admin>> listar() {
        log.info("GET /api/v1/admins");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/admins/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Admin> crear(@RequestBody Admin request) {
        log.info("POST /api/v1/admins");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> actualizar(@PathVariable Integer id, @RequestBody Admin request) {
        log.info("PUT /api/v1/admins/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/admins/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
