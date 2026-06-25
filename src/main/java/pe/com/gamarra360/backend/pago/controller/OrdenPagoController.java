package pe.com.gamarra360.backend.pago.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.entity.OrdenPagoDetalleResponse;
import pe.com.gamarra360.backend.pago.service.OrdenPagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes-pago")
@Slf4j
public class OrdenPagoController {
    private final OrdenPagoService service;

    public OrdenPagoController(OrdenPagoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OrdenPago>> listar() {
        log.info("GET /api/v1/ordenes-pago");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenPago> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/ordenes-pago/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<OrdenPago> crear(@RequestBody OrdenPago request) {
        log.info("POST /api/v1/ordenes-pago");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenPago> actualizar(@PathVariable Long id, @RequestBody OrdenPago request) {
        log.info("PUT /api/v1/ordenes-pago/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<OrdenPago>> listarPorCliente(@PathVariable Integer clienteId) {
        log.info("GET /api/v1/ordenes-pago/cliente/{}", clienteId);
        return ResponseEntity.ok(service.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<OrdenPagoDetalleResponse> obtenerDetalle(@PathVariable Long id) {
        log.info("GET /api/v1/ordenes-pago/{}/detalle", id);
        return ResponseEntity.ok(service.obtenerDetalle(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/ordenes-pago/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/buscar-por-intent/{paymentIntentId}")
    public ResponseEntity<Long> buscarPorPaymentIntent(@PathVariable String paymentIntentId) {
        log.info("GET /api/v1/ordenes-pago/buscar-por-intent/{}", paymentIntentId);
        return service.buscarOrdenIdPorPaymentIntent(paymentIntentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    //@PatchMapping("/{id}/marcar-pagado")
    //public ResponseEntity<Void> marcarPagado(@PathVariable Long id) {
        //log.info("PATCH /api/v1/ordenes-pago/{}/marcar-pagado", id);
        //service.marcarComoPagado(id);
        //return ResponseEntity.noContent().build();
    //}
}
