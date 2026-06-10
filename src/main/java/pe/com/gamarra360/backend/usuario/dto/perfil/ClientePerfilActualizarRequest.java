package pe.com.gamarra360.backend.usuario.dto.perfil;

public record ClientePerfilActualizarRequest(
        String nombres,
        String primerApellido,
        String segundoApellido,
        String nombre,
        String apellido,
        String telefono,
        String dni,
        String tipoDocumento,
        String direccionEntrega
) {}
