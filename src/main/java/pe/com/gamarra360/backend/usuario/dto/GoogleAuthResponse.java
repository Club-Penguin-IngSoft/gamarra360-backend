package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthResponse {
    private boolean registrado;
    
    // Si registrado == true (Login directo)
    private String token;
    private String tipo = "Bearer";
    private Integer usuarioId;
    private String email;
    private String rol;
    
    // Si registrado == false (Registro requerido, datos pre-completados de Google)
    private String nombres;
    private String primerApellido;
    private String googleEmail;

    public GoogleAuthResponse() {
    }
}
