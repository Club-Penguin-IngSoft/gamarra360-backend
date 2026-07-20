package pe.com.gamarra360.backend.solicitud.dto;

public record MensajePersonalizacionResponse(
        Long id,
        Integer remitenteId,
        String remitente,
        String mensaje,
        String fecha
) {}
