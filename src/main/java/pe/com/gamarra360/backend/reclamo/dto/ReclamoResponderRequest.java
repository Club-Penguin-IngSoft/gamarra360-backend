package pe.com.gamarra360.backend.reclamo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReclamoResponderRequest(@NotBlank @Size(max = 3000) String respuesta) {}
