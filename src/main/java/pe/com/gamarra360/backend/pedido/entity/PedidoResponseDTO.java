package pe.com.gamarra360.backend.pedido.entity;

public record PedidoResponseDTO(
        Long pedidoId,
        Long ordenPagoId,
        String estado,
        String estadoPago,
        Double total,
        Double costoEnvio,
        String nombreDistrito,
        String ciudad,
        String fechaEntregaEstimada   // "yyyy-MM-dd"
) {}
