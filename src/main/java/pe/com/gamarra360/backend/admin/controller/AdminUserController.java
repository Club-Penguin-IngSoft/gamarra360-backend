package pe.com.gamarra360.backend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.admin.dto.*;
import pe.com.gamarra360.backend.admin.service.AdminUserService;

/**
 * CU-03: Control y Gestión Centralizada de Usuarios
 *
 * Expone endpoints exclusivos para el rol ADMIN.
 * Cumple RF-11 (listar/filtrar usuarios) y RF-12 (activar/desactivar cuentas).
 *
 * Diseño deliberado:
 *  - Ningún endpoint modifica datos fuera del contexto de administración.
 *  - La desactivación es lógica (campo activo=false), nunca una eliminación física,
 *    preservando el historial de actividad (pedidos, cotizaciones, logs de auditoría).
 *  - El bloqueo es inmediato: el token JWT del usuario sigue siendo técnicamente válido
 *    hasta su expiración, pero el filtro JwtAuthenticationFilter llama a
 *    CustomUserDetailsService.loadUserByUsuario() en cada petición, y este lanza
 *    DisabledException si activo=false, rechazando la petición al instante.
 */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // -------------------------------------------------------------------------
    // RF-11: Listar usuarios con filtros por tipo (rol) y estado (activo/inactivo)
    // -------------------------------------------------------------------------

    /**
     * GET /api/v1/admin/usuarios
     *
     * Parámetros de filtro opcionales:
     *   rol    → CLIENTE | VENDEDOR | ADMIN
     *   activo → true | false
     *   q      → búsqueda libre por nombre, apellido o email
     *
     * Paginación gestionada por Spring Data (page, size, sort).
     */
    @GetMapping
    public ResponseEntity<Page<UsuarioResumenDTO>> listarUsuarios(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "usuarioId") Pageable pageable) {

        UsuarioFiltroDTO filtro = new UsuarioFiltroDTO(rol, activo, q);
        return ResponseEntity.ok(adminUserService.listarUsuarios(filtro, pageable));
    }

    /**
     * GET /api/v1/admin/usuarios/{id}
     *
     * Detalle completo de un usuario, incluyendo historial de actividad
     * (pedidos, cotizaciones, solicitudes). Solo lectura.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDetalleDTO> obtenerUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(adminUserService.obtenerDetalle(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResumenDTO> obtenerDashboard() {
        return ResponseEntity.ok(adminUserService.obtenerResumenDashboard());
    }
    // -------------------------------------------------------------------------
    // RF-12: Desactivar y reactivar cuentas
    // -------------------------------------------------------------------------

    /**
     * PATCH /api/v1/admin/usuarios/{id}/desactivar
     *
     * Desactiva la cuenta: activo=false.
     * Efecto inmediato: el usuario no puede autenticarse ni usar endpoints
     * protegidos en peticiones posteriores (ver Javadoc de clase).
     * El historial (pedidos, cotizaciones, logs) queda intacto.
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioEstadoDTO> desactivarUsuario(
            @PathVariable Integer id,
            @RequestBody MotivoDTO motivo) {

        return ResponseEntity.ok(adminUserService.desactivarUsuario(id, motivo.getRazon()));
    }

    /**
     * PATCH /api/v1/admin/usuarios/{id}/reactivar
     *
     * Reactiva la cuenta: activo=true.
     * El usuario puede volver a autenticarse inmediatamente.
     */
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<UsuarioEstadoDTO> reactivarUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(adminUserService.reactivarUsuario(id));
    }
}
