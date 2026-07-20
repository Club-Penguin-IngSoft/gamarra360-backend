package pe.com.gamarra360.backend.usuario.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.usuario.dto.ActualizarPerfilRequest;
import pe.com.gamarra360.backend.usuario.dto.CambiarPasswordRequest;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Slf4j
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> listar() {
        log.info("GET /api/v1/usuarios");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.usuarioId")
    public ResponseEntity<Usuario> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/usuarios/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> crear(@RequestBody Usuario request) {
        log.info("POST /api/v1/usuarios");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> actualizar(@PathVariable Integer id, @RequestBody Usuario request) {
        log.info("PUT /api/v1/usuarios/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/usuarios/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/perfil")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.usuarioId")
    public ResponseEntity<Void> actualizarPerfil(
            @PathVariable Integer id,
            @RequestBody ActualizarPerfilRequest request) {
        log.info("PATCH /api/v1/usuarios/{}/perfil", id);
        service.actualizarPerfil(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request,
                                                 Authentication authentication) {
        Integer usuarioId = ((UsuarioPrincipal) authentication.getPrincipal()).getUsuarioId();
        service.cambiarPassword(usuarioId, request);
        return ResponseEntity.noContent().build();
    }
}
