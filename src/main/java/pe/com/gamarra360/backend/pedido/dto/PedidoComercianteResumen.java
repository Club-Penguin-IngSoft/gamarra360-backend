package pe.com.gamarra360.backend.pedido.dto;

public record PedidoComercianteResumen(
        Long id,
        String fecha,
        String estado,
        Double total,
        Integer clienteId,
        String nombreCliente,
        String emailCliente
) {}
