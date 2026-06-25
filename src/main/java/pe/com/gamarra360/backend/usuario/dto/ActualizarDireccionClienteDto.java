package pe.com.gamarra360.backend.usuario.dto;

public record ActualizarDireccionClienteDto(
        String direccionEntrega,
        String referencia,
        Integer idDistrito
) {}
