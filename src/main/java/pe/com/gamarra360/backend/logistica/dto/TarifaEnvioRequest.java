package pe.com.gamarra360.backend.logistica.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
public record TarifaEnvioRequest(@NotNull Integer distritoId, @NotNull @PositiveOrZero Double costoEnvio, Boolean activo) {}
