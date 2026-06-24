package pe.com.gamarra360.backend.usuario.dto;

import jakarta.validation.constraints.NotBlank;

// DNI y email NO son editables (reglas KYC)
public record ActualizarDatosPersonalesDto(
        @NotBlank(message = "El nombre es obligatorio")
        String nombres,

        @NotBlank(message = "El primer apellido es obligatorio")
        String primerApellido,

        String segundoApellido,

        String telefono
) {}
