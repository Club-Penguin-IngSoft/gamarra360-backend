package pe.com.gamarra360.backend.usuario.dto;

public record ActualizarNotificacionesDto(
        boolean alertasCorreo,
        boolean notificacionesPush
) {}
