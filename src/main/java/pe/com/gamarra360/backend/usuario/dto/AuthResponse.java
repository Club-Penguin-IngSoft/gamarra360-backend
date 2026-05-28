package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private String tipo;
    private Integer usuarioId;
    private String email;
    private String nombres;
    private String rol;
    private boolean needsRegistration;

    public AuthResponse() {
    }

    // Login normal (email + contraseña)
    public AuthResponse(String token, Integer usuarioId, String email, String rol) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuarioId = usuarioId;
        this.email = email;
        this.nombres = null;
        this.rol = rol;
        this.needsRegistration = false;
    }

    // Google login — usuario ya existe o recién registrado
    public AuthResponse(String token, Integer usuarioId, String email, String nombres, String rol, boolean needsRegistration) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuarioId = usuarioId;
        this.email = email;
        this.nombres = nombres;
        this.rol = rol;
        this.needsRegistration = needsRegistration;
    }

    // Necesario para el caso needsRegistration=true (usuario no existe, sin token ni nombre)
    public AuthResponse(String token, Integer usuarioId, String email, String rol, boolean needsRegistration) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuarioId = usuarioId;
        this.email = email;
        this.nombres = null;
        this.rol = rol;
        this.needsRegistration = needsRegistration;
    }
}