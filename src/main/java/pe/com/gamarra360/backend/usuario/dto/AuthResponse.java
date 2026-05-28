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
    private String rol;
    private boolean needsRegistration;
    public AuthResponse() {
    }

    public AuthResponse(String token, Integer usuarioId, String email, String rol) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuarioId = usuarioId;
        this.email = email;
        this.rol = rol;
        this.needsRegistration = false;
    }

    public AuthResponse(String token, Integer usuarioId, String email, String rol, boolean needsRegistration) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuarioId = usuarioId;
        this.email = email;
        this.rol = rol;
        this.needsRegistration = needsRegistration;
    }
}
