package pe.com.gamarra360.backend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.admin.dto.*;
import pe.com.gamarra360.backend.admin.service.AdminVendorService;

/**
 * CU-04: Panel de Aprobación de Solicitudes de Vendedores
 *
 * Expone endpoints exclusivos para el rol ADMIN.
 * Cumple RF-14 (cola de solicitudes pendientes y aprobación/rechazo formal).
 *
 * Máquina de estados del vendedor (ver HU-9 del documento de arquitectura):
 *
 *   PENDIENTE_VERIFICACION  →  (email confirmado)  →  PENDIENTE_APROBACION
 *   PENDIENTE_APROBACION    →  (admin aprueba)     →  APROBADO
 *   PENDIENTE_APROBACION    →  (admin rechaza)     →  RECHAZADO
 *   APROBADO                →  (admin suspende)    →  SUSPENDIDO
 *   SUSPENDIDO              →  (admin reactiva)    →  APROBADO
 *
 * Cada transición de estado dispara un evento de dominio que:
 *  1. Persiste el cambio en la entidad Comerciante (verificado, activo).
 *  2. Publica una notificación asíncrona vía @Async al email del comerciante.
 *  3. Si el estado es APROBADO, habilita la Tienda y asigna el tenant_id.
 *
 * El controlador NO contiene lógica de negocio. Delega todo al AdminVendorService.
 */
@RestController
@RequestMapping("/api/v1/admin/vendedores")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminVendorController {

    private final AdminVendorService adminVendorService;

    // -------------------------------------------------------------------------
    // RF-14: Cola de solicitudes pendientes (redireccionamiento prioritario)
    // -------------------------------------------------------------------------

    /**
     * GET /api/v1/admin/vendedores/pendientes
     *
     * Devuelve la cola de comerciantes en estado PENDIENTE_APROBACION,
     * ordenados por fecha de solicitud ascendente (los más antiguos primero).
     * Este es el endpoint al que el administrador es redirigido al iniciar sesión
     * si existen solicitudes en espera (lógica de redirección en el frontend).
     */
    @GetMapping("/pendientes")
    public ResponseEntity<Page<SolicitudVendedorDTO>> listarPendientes(
            @PageableDefault(size = 10, sort = "usuarioId") Pageable pageable) {

        return ResponseEntity.ok(adminVendorService.listarPendientes(pageable));
    }

    /**
     * GET /api/v1/admin/vendedores/pendientes/conteo
     *
     * Retorna el número de solicitudes pendientes.
     * Usado por el frontend para mostrar el badge de notificación en el panel
     * y para decidir el redireccionamiento prioritario al login del admin.
     */
    @GetMapping("/pendientes/conteo")
    public ResponseEntity<ConteoDTO> contarPendientes() {
        return ResponseEntity.ok(adminVendorService.contarPendientes());
    }

    /**
     * GET /api/v1/admin/vendedores/{comercianteId}
     *
     * Detalle completo de la solicitud: datos del comerciante, RUC,
     * razón social y documentación adjunta para revisión del administrador.
     */
    @GetMapping("/{comercianteId}")
    public ResponseEntity<SolicitudVendedorDetalleDTO> obtenerSolicitud(
            @PathVariable Integer comercianteId) {

        return ResponseEntity.ok(adminVendorService.obtenerDetalleSolicitud(comercianteId));
    }

    // -------------------------------------------------------------------------
    // RF-14: Aprobación y rechazo formal de solicitudes
    // -------------------------------------------------------------------------

    /**
     * POST /api/v1/admin/vendedores/{comercianteId}/aprobar
     *
     * Transición: PENDIENTE_APROBACION → APROBADO
     *
     * Efectos de dominio (orquestados en AdminVendorService, dentro de @Transactional):
     *  1. Comerciante.verificado = true, activo = true.
     *  2. Se crea/activa la Tienda asociada con su tenant_id.
     *  3. El rol del Usuario asociado permanece VENDEDOR (ya asignado en registro).
     *  4. Se dispara notificación asíncrona de aprobación al email del comerciante.
     *  5. Se registra el evento en el log de auditoría (quién aprobó, cuándo).
     */
    @PostMapping("/{comercianteId}/aprobar")
    public ResponseEntity<RespuestaAprobacionDTO> aprobarVendedor(
            @PathVariable Integer comercianteId) {

        return ResponseEntity.ok(adminVendorService.aprobarVendedor(comercianteId));
    }

    /**
     * POST /api/v1/admin/vendedores/{comercianteId}/rechazar
     *
     * Transición: PENDIENTE_APROBACION → RECHAZADO
     *
     * El cuerpo de la petición debe incluir el motivo del rechazo.
     * Este motivo se persiste en el campo mensaje_rechazo_cliente de la Solicitud
     * y se incluye en la notificación enviada al comerciante.
     *
     * Efectos:
     *  1. Comerciante.verificado = false, activo = false.
     *  2. El usuario puede volver a postular (flujo de re-solicitud en front).
     *  3. Se dispara notificación con el motivo al email del comerciante.
     *  4. Se registra en el log de auditoría.
     */
    @PostMapping("/{comercianteId}/rechazar")
    public ResponseEntity<RespuestaAprobacionDTO> rechazarVendedor(
            @PathVariable Integer comercianteId,
            @RequestBody MotivoDTO motivo) {

        return ResponseEntity.ok(adminVendorService.rechazarVendedor(comercianteId, motivo.getRazon()));
    }

    /**
     * POST /api/v1/admin/vendedores/{comercianteId}/suspender
     *
     * Transición: APROBADO → SUSPENDIDO
     *
     * Suspensión temporal de un comerciante ya aprobado.
     * La tienda queda inactiva, sus productos no son visibles en el catálogo,
     * pero el historial de pedidos y datos permanecen intactos.
     */
//    @PostMapping("/{comercianteId}/suspender")
//    public ResponseEntity<RespuestaAprobacionDTO> suspenderVendedor(
//            @PathVariable Integer comercianteId,
//            @RequestBody MotivoDTO motivo) {
//
//        return ResponseEntity.ok(adminVendorService.suspenderVendedor(comercianteId, motivo.getRazon()));
//    }

    /**
     * GET /api/v1/admin/vendedores
     *
     * Lista todos los comerciantes con filtros opcionales por estado.
     * Para auditoría y seguimiento del ciclo de vida completo del vendedor.
     */
    @GetMapping
    public ResponseEntity<Page<SolicitudVendedorDTO>> listarTodos(
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(adminVendorService.listarTodos(estado, pageable));
    }
}
