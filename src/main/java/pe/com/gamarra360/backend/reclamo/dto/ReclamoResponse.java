package pe.com.gamarra360.backend.reclamo.dto;

public record ReclamoResponse(
        Long id, Integer clienteId, String nombreCliente, Integer vendedorId,
        Long pedidoId, String tipo, String asunto, String descripcion,
        String estado, String respuesta, String fechaCreacion, String fechaRespuesta
) {}
