package pe.com.gamarra360.backend.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensajePersonalizacionRequest(
        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 1500, message = "El mensaje no puede superar 1500 caracteres")
        String mensaje
) {}
