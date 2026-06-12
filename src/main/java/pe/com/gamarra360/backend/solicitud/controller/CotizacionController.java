package pe.com.gamarra360.backend.solicitud.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import pe.com.gamarra360.backend.solicitud.dto.*;
import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;
import pe.com.gamarra360.backend.solicitud.service.CotizacionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cotizaciones")
@Slf4j
public class CotizacionController {

    private final CotizacionService service;

    public CotizacionController(CotizacionService service) {
        this.service = service;
    }

    /* ── CRUD base ────────────────────────────────────────────────────── */

    @GetMapping
    public ResponseEntity<List<Cotizacion>> listar() {
        log.info("GET /api/v1/cotizaciones");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cotizacion> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/cotizaciones/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cotizacion> actualizar(@PathVariable Long id, @RequestBody Cotizacion request) {
        log.info("PUT /api/v1/cotizaciones/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/cotizaciones/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /* ── Negocio: crear solicitud ─────────────────────────────────────── */

    @PostMapping
    public ResponseEntity<CotizacionDetalleResponse> crear(
            @Valid @RequestBody CotizacionRequest request,
            Authentication auth) {
        Integer clienteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("POST /api/v1/cotizaciones — cliente {}", clienteId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crearSolicitud(request, clienteId));
    }

    /* ── Negocio: vista del cliente ───────────────────────────────────── */

    @GetMapping("/mis-cotizaciones")
    public ResponseEntity<List<CotizacionResumen>> misCotizaciones(Authentication auth) {
        Integer clienteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("GET /api/v1/cotizaciones/mis-cotizaciones — cliente {}", clienteId);
        return ResponseEntity.ok(service.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<CotizacionDetalleResponse> obtenerDetalle(@PathVariable Long id) {
        log.info("GET /api/v1/cotizaciones/{}/detalle", id);
        return ResponseEntity.ok(service.obtenerDetalle(id));
    }

    @PatchMapping("/{id}/aceptar")
    public ResponseEntity<CotizacionDetalleResponse> aceptar(
            @PathVariable Long id, Authentication auth) {
        Integer clienteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("PATCH /api/v1/cotizaciones/{}/aceptar — cliente {}", id, clienteId);
        return ResponseEntity.ok(service.aceptar(id, clienteId));
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazar(
            @PathVariable Long id, Authentication auth) {
        Integer clienteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("PATCH /api/v1/cotizaciones/{}/rechazar — cliente {}", id, clienteId);
        service.rechazar(id, clienteId);
        return ResponseEntity.noContent().build();
    }

    /* ── Negocio: vista del comerciante ───────────────────────────────── */

    @GetMapping("/comerciante")
    public ResponseEntity<List<CotizacionResumen>> cotizacionesComerciante(Authentication auth) {
        Integer vendedorId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("GET /api/v1/cotizaciones/comerciante — vendedor {}", vendedorId);
        return ResponseEntity.ok(service.listarPorVendedor(vendedorId));
    }

    @PostMapping("/{id}/responder")
    public ResponseEntity<CotizacionDetalleResponse> responder(
            @PathVariable Long id,
            @RequestBody RespuestaCotizacionRequest request,
            Authentication auth) {
        Integer vendedorId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("POST /api/v1/cotizaciones/{}/responder — vendedor {}", id, vendedorId);
        return ResponseEntity.ok(service.responder(id, request, vendedorId));
    }
}
