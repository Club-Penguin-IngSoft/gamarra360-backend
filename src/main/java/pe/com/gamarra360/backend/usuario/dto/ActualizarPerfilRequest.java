package pe.com.gamarra360.backend.usuario.dto;

public record ActualizarPerfilRequest(
        String nombres,
        String primerApellido,
        String segundoApellido,
        String telefono,
        String direccionEntrega
) {}
