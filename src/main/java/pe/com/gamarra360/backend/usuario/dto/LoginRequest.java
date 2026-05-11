package pe.com.gamarra360.backend.usuario.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String contrasenha;
}
