package pe.com.gamarra360.backend.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminUsuarioActualizarRequest(
        @NotBlank String nombres,
        @NotBlank String primerApellido,
        String segundoApellido,
        @Email @NotBlank String email,
        String dni,
        String telefono
) {}
