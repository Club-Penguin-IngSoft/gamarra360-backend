package pe.com.gamarra360.backend.usuario.dto.perfil;

public record ClientePreferenciasNotificacionDto(
        Boolean alertasCorreo,
        Boolean notificacionesPush
) {}
