package pe.com.gamarra360.backend.pedido.entity;

public record PedidoResponseDTO(
        Long pedidoId,
        Long ordenPagoId,
        String estado,        // "RECIBIDO"
        String estadoPago,    // "PAGADO"
        Double total
) {}
