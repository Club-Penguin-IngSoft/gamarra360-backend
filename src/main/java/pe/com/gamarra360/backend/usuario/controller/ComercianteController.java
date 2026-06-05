package pe.com.gamarra360.backend.usuario.controller;

import com.stripe.model.Account;
import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.service.ComercianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;
import pe.com.gamarra360.backend.usuario.dto.OnboardingLinkResponse;
import pe.com.gamarra360.backend.usuario.service.ComercianteStripeService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comerciantes")
@Slf4j
public class ComercianteController {
    private final ComercianteService service;
    private final ComercianteStripeService stripeService;
    public ComercianteController(ComercianteService service, ComercianteStripeService stripeService) {
        this.service = service;
        this.stripeService=stripeService;
    }

    @GetMapping
    public ResponseEntity<List<Comerciante>> listar() {
        log.info("GET /api/v1/comerciantes");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Comerciante>> listarPendientes() {
        log.info("GET /api/v1/comerciantes/pendientes");
        return ResponseEntity.ok(service.listarPendientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comerciante> obtener(@PathVariable Integer id) {
        log.info("GET /api/v1/comerciantes/{}", id);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Comerciante> crear(@RequestBody Comerciante request) {
        log.info("POST /api/v1/comerciantes");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comerciante> actualizar(@PathVariable Integer id, @RequestBody Comerciante request) {
        log.info("PUT /api/v1/comerciantes/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Comerciante> aprobar(@PathVariable Integer id) {
        log.info("PATCH /api/v1/comerciantes/{}/aprobar", id);
        return ResponseEntity.ok(service.aprobar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/comerciantes/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rechazar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/comerciantes/{}/rechazar", id);
        service.rechazar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stripe/onboarding")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<OnboardingLinkResponse> generarOnboarding(
            @PathVariable Integer id) {
        log.info("POST /api/v1/comerciantes/{}/stripe/onboarding", id);
        try {
            return ResponseEntity.ok(stripeService.generarLinkOnboarding(id));
        } catch (StripeException e) {
            log.error("Error generando onboarding Stripe para comerciante {}: {}",
                    id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Stripe redirige aquí cuando el comerciante completa el formulario
    @GetMapping("/stripe/completado")
    public ResponseEntity<String> onboardingCompletado(
            @RequestParam String account) {
        log.info("GET /api/v1/comerciantes/stripe/completado - account: {}", account);
        try {
            stripeService.completarOnboarding(account);
            return ResponseEntity.ok("Onboarding completado correctamente.");
        } catch (StripeException e) {
            log.error("Error completando onboarding: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/stripe/balance")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<?> obtenerBalance(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(stripeService.obtenerBalance(id));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/stripe/status")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> stripeStatus(@PathVariable Integer id) {
        try {
            Comerciante comerciante = service.obtener(id);
            if (comerciante.getStripeAccountId() == null) {
                return ResponseEntity.ok(Map.of("yaCompletado", false));
            }
            Account account = Account.retrieve(comerciante.getStripeAccountId());
            return ResponseEntity.ok(Map.of(
                    "yaCompletado", Boolean.TRUE.equals(account.getDetailsSubmitted())
            ));
        } catch (StripeException e) {
            return ResponseEntity.ok(Map.of("yaCompletado", false));
        }
    }
    // ComercianteController.java
    @GetMapping("/{id}/stripe/dashboard")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, String>> obtenerDashboardLink(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(stripeService.generarDashboardLink(id));
        } catch (StripeException e) {
            log.error("Error generando dashboard Stripe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}