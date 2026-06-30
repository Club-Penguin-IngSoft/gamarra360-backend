package pe.com.gamarra360.backend.usuario.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.gamarra360.backend.usuario.dto.RestablecerPasswordRequest;
import pe.com.gamarra360.backend.usuario.dto.SolicitarCodigoRequest;
import pe.com.gamarra360.backend.usuario.dto.VerificarCodigoRequest;
import pe.com.gamarra360.backend.usuario.service.RecuperacionService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/recuperar")
@RequiredArgsConstructor
@Slf4j
public class RecuperacionController {

    private final RecuperacionService service;

    /**
     * POST /api/v1/auth/recuperar/solicitar
     * Body: { "email": "usuario@gmail.com" }
     * Valida que exista y sea LOCAL, luego envía el código por correo.
     */
    @PostMapping("/solicitar")
    public ResponseEntity<Map<String, String>> solicitar(
            @RequestBody SolicitarCodigoRequest request) {
        log.info("POST /recuperar/solicitar email={}", request.email());
        service.solicitarCodigo(request.email());
        return ResponseEntity.ok(Map.of("mensaje", "Código enviado a tu correo."));
    }

    /**
     * POST /api/v1/auth/recuperar/verificar
     * Body: { "email": "...", "codigo": "123456" }
     * Devuelve 200 si el código es válido, 400 si no.
     */
    @PostMapping("/verificar")
    public ResponseEntity<Map<String, String>> verificar(
            @RequestBody VerificarCodigoRequest request) {
        log.info("POST /recuperar/verificar email={}", request.email());
        service.verificarCodigo(request.email(), request.codigo());
        return ResponseEntity.ok(Map.of("mensaje", "Código verificado correctamente."));
    }

    /**
     * POST /api/v1/auth/recuperar/restablecer
     * Body: { "email": "...", "codigo": "123456", "nuevaContrasenha": "..." }
     */
    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecer(
            @RequestBody RestablecerPasswordRequest request) {
        log.info("POST /recuperar/restablecer email={}", request.email());
        service.restablecerPassword(request.email(), request.codigo(), request.nuevaContrasenha());
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
    }
}
