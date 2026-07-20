package pe.com.gamarra360.backend.logistica.dto;
public record TarifaEnvioResponse(Long id, Integer distritoId, String distrito, Double costoEnvio, Boolean activo) {}
