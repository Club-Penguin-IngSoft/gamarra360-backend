package pe.com.gamarra360.backend.pedido.dto;

import java.util.List;

public record PedidoComercianteDetalle(
        Long id,
        String fecha,
        String fechaActualizacion,
        String estado,
        Double total,
        String tipoEntrega,
        String direccionEntrega,
        Integer clienteId,
        String nombreCliente,
        String emailCliente,
        List<ItemDetalle> items,
        List<HistorialPedido> historialCliente
) {
    public record ItemDetalle(
            Long id,
            Integer idVarianteProducto,
            String nombreProducto,
            String imagenUrl,
            String talla,
            String color,
            String sku,
            Integer cantidad,
            Double precio,
            PersonalizacionInfo personalizacion
    ) {}

    public record PersonalizacionInfo(
            Long id,
            String tipoPersonalizacion,
            String descripcion,
            String urlLogo,
            Integer cantidad
    ) {}

    public record HistorialPedido(
            Long id,
            String fecha,
            String estado,
            Double total
    ) {}
}
