package pe.com.gamarra360.backend.reclamo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReclamoCrearRequest(
        Long pedidoId,
        @NotBlank @Pattern(regexp = "RECLAMO_PEDIDO|LIBRO_PLATAFORMA") String tipo,
        @NotBlank @Size(max = 200) String asunto,
        @NotBlank @Size(max = 3000) String descripcion
) {}
