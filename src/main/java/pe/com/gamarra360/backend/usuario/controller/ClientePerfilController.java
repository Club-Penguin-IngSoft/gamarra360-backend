package pe.com.gamarra360.backend.usuario.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import pe.com.gamarra360.backend.usuario.dto.perfil.*;
import pe.com.gamarra360.backend.usuario.service.ClientePerfilService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes/me")
@Slf4j
public class ClientePerfilController {

    private final ClientePerfilService service;

    public ClientePerfilController(ClientePerfilService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public ResponseEntity<ClienteResumenCuentaResponse> obtenerResumenCuenta(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        log.info("GET /api/v1/clientes/me/resumen");
        return ResponseEntity.ok(service.obtenerResumenCuenta(principal));
    }

    @GetMapping("/perfil")
    public ResponseEntity<ClientePerfilResponse> obtenerPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        log.info("GET /api/v1/clientes/me/perfil");
        return ResponseEntity.ok(service.obtenerPerfil(principal));
    }

    @PutMapping("/perfil")
    public ResponseEntity<ClientePerfilResponse> actualizarPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody ClientePerfilActualizarRequest request) {
        log.info("PUT /api/v1/clientes/me/perfil");
        return ResponseEntity.ok(service.actualizarPerfil(principal, request));
    }

    @PatchMapping("/direccion")
    public ResponseEntity<ClientePerfilResponse> actualizarDireccion(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody ClienteDireccionRequest request) {
        log.info("PATCH /api/v1/clientes/me/direccion");
        return ResponseEntity.ok(service.actualizarDireccion(principal, request));
    }

    @PatchMapping("/preferencias-notificacion")
    public ResponseEntity<ClientePreferenciasNotificacionDto> actualizarPreferencias(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody ClientePreferenciasNotificacionRequest request) {
        log.info("PATCH /api/v1/clientes/me/preferencias-notificacion");
        return ResponseEntity.ok(service.actualizarPreferencias(principal, request));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> cambiarPassword(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody ClienteCambiarPasswordRequest request) {
        log.info("PATCH /api/v1/clientes/me/password");
        service.cambiarPassword(principal, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cuenta")
    public ResponseEntity<Void> desactivarCuenta(@AuthenticationPrincipal UsuarioPrincipal principal) {
        log.info("DELETE /api/v1/clientes/me/cuenta");
        service.desactivarCuenta(principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<ClientePedidoResumenDto>> listarPedidos(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        log.info("GET /api/v1/clientes/me/pedidos");
        return ResponseEntity.ok(service.listarPedidos(principal));
    }

    @GetMapping("/pedidos/recientes")
    public ResponseEntity<List<ClientePedidoResumenDto>> listarPedidosRecientes(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestParam(defaultValue = "2") int limite) {
        log.info("GET /api/v1/clientes/me/pedidos/recientes?limite={}", limite);
        return ResponseEntity.ok(service.listarPedidosRecientes(principal, limite));
    }

    @GetMapping("/pedidos/{pedidoId}")
    public ResponseEntity<ClientePedidoDetalleDto> obtenerPedido(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable Long pedidoId) {
        log.info("GET /api/v1/clientes/me/pedidos/{}", pedidoId);
        return ResponseEntity.ok(service.obtenerPedido(principal, pedidoId));
    }

    @PatchMapping("/pedidos/{pedidoId}/cancelar")
    public ResponseEntity<ClientePedidoDetalleDto> cancelarPedido(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable Long pedidoId) {
        log.info("PATCH /api/v1/clientes/me/pedidos/{}/cancelar", pedidoId);
        return ResponseEntity.ok(service.cancelarPedido(principal, pedidoId));
    }

    @GetMapping("/personalizaciones")
    public ResponseEntity<List<ClienteSolicitudResumenDto>> listarPersonalizaciones(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        log.info("GET /api/v1/clientes/me/personalizaciones");
        return ResponseEntity.ok(service.listarPersonalizaciones(principal));
    }

    @GetMapping("/personalizaciones/{personalizacionId}")
    public ResponseEntity<ClienteSolicitudResumenDto> obtenerPersonalizacion(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable Long personalizacionId) {
        log.info("GET /api/v1/clientes/me/personalizaciones/{}", personalizacionId);
        return ResponseEntity.ok(service.obtenerPersonalizacion(principal, personalizacionId));
    }

    @GetMapping("/cotizaciones")
    public ResponseEntity<List<ClienteSolicitudResumenDto>> listarCotizaciones(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        log.info("GET /api/v1/clientes/me/cotizaciones");
        return ResponseEntity.ok(service.listarCotizaciones(principal));
    }

    @GetMapping("/cotizaciones/{cotizacionId}")
    public ResponseEntity<ClienteSolicitudResumenDto> obtenerCotizacion(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable Long cotizacionId) {
        log.info("GET /api/v1/clientes/me/cotizaciones/{}", cotizacionId);
        return ResponseEntity.ok(service.obtenerCotizacion(principal, cotizacionId));
    }
}
