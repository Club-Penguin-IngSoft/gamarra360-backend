package pe.com.gamarra360.backend.usuario.dto.perfil;

public record ClientePreferenciasNotificacionRequest(
        Boolean alertasCorreo,
        Boolean notificacionesPush
) {}
