package pe.com.gamarra360.backend.pedido.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteDetalle;
import pe.com.gamarra360.backend.pedido.dto.PedidoComercianteResumen;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.service.PedidoService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@Slf4j
public class PedidoController {
    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        log.info("GET /api/v1/pedidos");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/pedidos/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody Pedido request) {
        log.info("POST /api/v1/pedidos - tipoEntrega: {}, direccionEntrega: {}, total: {}",
                request.getTipoEntrega(), request.getDireccionEntrega(), request.getTotal());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> actualizar(@PathVariable Long id, @RequestBody Pedido request) {
        log.info("PUT /api/v1/pedidos/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/pedidos/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Pedido> cancelar(@PathVariable Long id, Authentication auth) {
        Integer clienteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("PATCH /api/v1/pedidos/{}/cancelar — clienteId={}", id, clienteId);
        service.cancelar(id, clienteId);
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/comerciante")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<List<PedidoComercianteResumen>> pedidosComerciante(Authentication auth) {
        Integer vendedorId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("GET /api/v1/pedidos/comerciante — vendedorId={}", vendedorId);
        return ResponseEntity.ok(service.listarPorVendedor(vendedorId));
    }

    @GetMapping("/{id}/comerciante-detalle")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<PedidoComercianteDetalle> pedidoComercianteDetalle(@PathVariable Long id, Authentication auth) {
        Integer vendedorId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("GET /api/v1/pedidos/{}/comerciante-detalle — vendedorId={}", id, vendedorId);
        return ResponseEntity.ok(service.obtenerDetalleComerciante(id, vendedorId));
    }
}
