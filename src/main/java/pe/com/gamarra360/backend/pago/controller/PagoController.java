package pe.com.gamarra360.backend.pago.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeRequest;
import pe.com.gamarra360.backend.pago.dto.CrearPagoStripeResponse;
import pe.com.gamarra360.backend.pago.dto.PrepararCarritoRequest;
import pe.com.gamarra360.backend.pago.dto.PrepararCarritoResponse;
import pe.com.gamarra360.backend.pago.entity.Pago;
import pe.com.gamarra360.backend.pago.service.PagoService;
import pe.com.gamarra360.backend.pago.service.impl.StripePaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@Slf4j
public class PagoController {

    private final PagoService          service;
    private final StripePaymentService stripePaymentService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;
    @Value("${stripe.webhook-secret-connect:}")
    private String webhookSecretConnect;

    public PagoController(PagoService service, StripePaymentService stripePaymentService) {
        this.service               = service;
        this.stripePaymentService  = stripePaymentService;
    }

    @GetMapping
    public ResponseEntity<List<Pago>> listar() {
        log.info("GET /api/v1/pagos");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtener(@PathVariable Long id) {
        log.info("GET /api/v1/pagos/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Pago> crear(@RequestBody Pago request) {
        log.info("POST /api/v1/pagos");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pago> actualizar(@PathVariable Long id, @RequestBody Pago request) {
        log.info("PUT /api/v1/pagos/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/pagos/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preparar")
    public ResponseEntity<PrepararCarritoResponse> prepararCarrito(
            @RequestBody PrepararCarritoRequest request) {
        log.info("POST /api/v1/pagos/preparar - clienteId: {}", request.getClienteId());
        try {
            return ResponseEntity.ok(stripePaymentService.prepararCarrito(request));
        } catch (Exception e) {
            log.error("Error preparando carrito: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/crear-intent")
    public ResponseEntity<CrearPagoStripeResponse> crearIntent(
            @RequestBody CrearPagoStripeRequest request) {
        log.info("POST /api/v1/pagos/crear-intent - carritoPendienteId: {}", request.carritoPendienteId());
        try {
            return ResponseEntity.ok(stripePaymentService.crearPaymentIntent(request));
        } catch (StripeException e) {
            log.error("Stripe error code: {} mensaje: {}", e.getCode(), e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (Exception e) {
            log.error("Error inesperado al crear intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("POST /api/v1/pagos/webhook — tipo evento pendiente de parsear");
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Evento Stripe recibido: {}", event.getType());

            if ("payment_intent.succeeded".equals(event.getType())) {
                log.info("Procesando payment_intent.succeeded");

                EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                PaymentIntent intent;
                if (deserializer.getObject().isPresent()) {
                    intent = (PaymentIntent) deserializer.getObject().get();
                } else {
                    log.warn("Deserializer vacío, usando deserializeUnsafe");
                    intent = (PaymentIntent) deserializer.deserializeUnsafe();
                }

                log.info("Intent id={} metadata={}", intent.getId(), intent.getMetadata());
                stripePaymentService.procesarPagoConfirmado(intent);
            }

            return ResponseEntity.ok("OK");
        } catch (SignatureVerificationException e) {
            log.warn("Firma webhook inválida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma inválida");
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}