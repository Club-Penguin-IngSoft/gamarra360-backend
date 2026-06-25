package pe.com.gamarra360.backend.usuario.dto;

public record PerfilClienteDto(
        // Identidad (de tabla usuarios)
        String email,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String tipoDocumento,
        String dni,
        String telefono,
        // Logística (de tabla clientes)
        String direccionEntrega,
        String referencia,
        Integer idDistrito,
        String nombreDistrito,
        String ciudadDistrito,
        // Preferencias de notificación
        Boolean alertasCorreo,
        Boolean notificacionesPush
) {}
