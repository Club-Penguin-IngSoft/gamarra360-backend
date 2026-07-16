package pe.com.gamarra360.backend.usuario.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import pe.com.gamarra360.backend.usuario.dto.ActualizarDatosPersonalesDto;
import pe.com.gamarra360.backend.usuario.dto.ActualizarDireccionClienteDto;
import pe.com.gamarra360.backend.usuario.dto.ActualizarNotificacionesDto;
import pe.com.gamarra360.backend.usuario.dto.PerfilClienteDto;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.service.ClienteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@Slf4j
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    // ── Mi Cuenta ─────────────────────────────────────────────────────────

    @GetMapping("/perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PerfilClienteDto> obtenerPerfil(Authentication auth) {
        Integer usuarioId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("GET /api/v1/clientes/perfil — usuarioId={}", usuarioId);
        return ResponseEntity.ok(service.obtenerPerfil(usuarioId));
    }

    @PutMapping("/perfil/datos-personales")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> actualizarDatosPersonales(
            @Valid @RequestBody ActualizarDatosPersonalesDto dto,
            Authentication auth) {
        Integer usuarioId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("PUT /api/v1/clientes/perfil/datos-personales — usuarioId={}", usuarioId);
        service.actualizarDatosPersonales(usuarioId, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/perfil/notificaciones")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PerfilClienteDto> actualizarNotificaciones(
            @RequestBody ActualizarNotificacionesDto dto,
            Authentication auth) {
        Integer usuarioId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("PUT /api/v1/clientes/perfil/notificaciones — usuarioId={}", usuarioId);
        return ResponseEntity.ok(service.actualizarNotificaciones(usuarioId, dto));
    }

    @PutMapping("/perfil/direccion")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> actualizarDireccion(
            @RequestBody ActualizarDireccionClienteDto dto,
            Authentication auth) {
        Integer usuarioId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("PUT /api/v1/clientes/perfil/direccion — usuarioId={}", usuarioId);
        service.actualizarDireccion(usuarioId, dto);
        return ResponseEntity.noContent().build();
    }

    // ── CRUD interno (admin) ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@RequestBody Cliente request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Integer id, @RequestBody Cliente request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
