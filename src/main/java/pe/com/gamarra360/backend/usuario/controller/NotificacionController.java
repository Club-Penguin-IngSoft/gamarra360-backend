package pe.com.gamarra360.backend.usuario.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.usuario.entity.Notificacion;
import pe.com.gamarra360.backend.usuario.service.NotificacionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@Slf4j
public class NotificacionController {
    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        log.info("GET /api/v1/notificaciones");
        return ResponseEntity.ok(service.listar());
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Notificacion> obtener(@PathVariable Integer id) {
//        log.info("GET /api/v1/notificaciones/{}", id);
//        return ResponseEntity.ok(service.obtener(id));
//    }
//    @GetMapping("/usuario/{usuarioId}")
//    public ResponseEntity<List<Notificacion>> obtenerPorUsuario(@PathVariable Integer usuarioId) {
//        log.info("GET /api/v1/notificaciones/usuario/{}", usuarioId);
//        return ResponseEntity.ok(service.obtenerPorUsuarioId(usuarioId));
//    }
//
//    @PostMapping
//    public ResponseEntity<Notificacion> crear(@RequestBody Notificacion request) {
//        log.info("POST /api/v1/notificaciones");
//        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<Notificacion> actualizar(@PathVariable Integer id, @RequestBody Notificacion request) {
        log.info("PUT /api/v1/notificaciones/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/notificaciones/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
//    @GetMapping("/usuario/{id}")
//    public List<Notificacion> listar(@PathVariable Integer id) {
//        return service.listarPorUsuario(id);
//    }
//    @PutMapping("/leida/{id}")
//    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable Integer id) {
//        log.info("PUT /api/v1/notificaciones/leida/{}", id);
//        Notificacion notificacion = service.obtener(id);
//        notificacion.setFueleida(true);
//        return ResponseEntity.ok(service.actualizar(id, notificacion));
//    }
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> porUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    @PutMapping("/leida/{id}")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Integer id) {

        Notificacion n = service.obtener(id);

        if (n == null) {
            throw new RuntimeException("No existe");
        }

        n.setFueleida(true);

        return ResponseEntity.ok(service.actualizar(id, n));
    }
}
