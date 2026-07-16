package pe.com.gamarra360.backend.logistica.dto;

public record DistritoEnvioDto(
        Integer id,
        String ciudad,
        String nombre,
        Double costoEnvio
) {}
