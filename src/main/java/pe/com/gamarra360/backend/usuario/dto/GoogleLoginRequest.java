package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class GoogleLoginRequest {
    @NotBlank(message = "El token de Google es obligatorio")
    private String idToken;
}
