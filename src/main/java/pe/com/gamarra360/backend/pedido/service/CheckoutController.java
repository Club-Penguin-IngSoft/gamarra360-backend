package pe.com.gamarra360.backend.pedido.service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.pedido.entity.PedidoRequestDTO;
import pe.com.gamarra360.backend.pedido.entity.PedidoResponseDTO;
import pe.com.gamarra360.backend.pedido.service.CheckoutService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;

/**
 * Endpoint de checkout con pago simulado.
 *
 * POST /api/v1/checkout
 *  - Requiere rol CLIENTE (usuario autenticado)
 *  - El clienteId se extrae del JWT — nunca del body
 *  - Devuelve 201 Created con el resumen del pedido
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponseDTO> checkout(
            @Valid @RequestBody PedidoRequestDTO dto,
            Authentication auth) {

        Integer clienteId = ((UsuarioPrincipal) auth.getPrincipal()).getUsuarioId();
        log.info("POST /api/v1/checkout — clienteId={}", clienteId);

        PedidoResponseDTO response = checkoutService.procesarCompra(dto, clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
