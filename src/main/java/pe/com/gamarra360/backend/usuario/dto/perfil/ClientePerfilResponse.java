package pe.com.gamarra360.backend.usuario.dto.perfil;

public record ClientePerfilResponse(
        Integer usuarioId,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String nombreCompleto,
        String email,
        String telefono,
        String dni,
        String tipoDocumento,
        String rol,
        String direccionEntrega,
        ClientePreferenciasNotificacionDto preferenciasNotificacion
) {}
