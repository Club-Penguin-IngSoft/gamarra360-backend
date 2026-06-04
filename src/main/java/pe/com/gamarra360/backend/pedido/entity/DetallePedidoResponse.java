package pe.com.gamarra360.backend.pedido.entity;

public record DetallePedidoResponse(
        Long id,
        Long pedidoId,
        Integer idVarianteProducto,
        Integer cantidad,
        Double precio,
        String nombreProducto,
        String talla,
        String color,
        String sku
) {}
