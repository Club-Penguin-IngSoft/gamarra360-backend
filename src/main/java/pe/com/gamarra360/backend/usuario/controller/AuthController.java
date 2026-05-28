package pe.com.gamarra360.backend.usuario.controller;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.usuario.dto.AuthResponse;
import pe.com.gamarra360.backend.usuario.dto.LoginRequest;
import pe.com.gamarra360.backend.usuario.dto.RegistroUsuarioRequest;
import pe.com.gamarra360.backend.usuario.dto.GoogleLoginRequest;
import pe.com.gamarra360.backend.usuario.dto.GoogleAuthResponse;
import pe.com.gamarra360.backend.usuario.dto.GoogleRegistroRequest;
import pe.com.gamarra360.backend.usuario.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        log.info("POST /api/v1/auth/register");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login");
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponse> autenticarConGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        log.info("POST /api/v1/auth/google");
        return ResponseEntity.ok(authService.autenticarConGoogle(request));
    }

    @PostMapping("/google/register")
    public ResponseEntity<AuthResponse> registrarConGoogle(@Valid @RequestBody GoogleRegistroRequest request) {
        log.info("POST /api/v1/auth/google/register");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarConGoogle(request));
    }
}
