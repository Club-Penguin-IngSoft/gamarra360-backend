package pe.com.gamarra360.backend.pago.entity;

import java.util.List;

public record OrdenPagoDetalleResponse(
        Long id,
        Integer clienteId,
        Double total,
        String estado,
        String fecha,
        List<PedidoResumen> pedidos
) {
    public record PedidoResumen(
            Long id,
            Integer vendedorId,
            String nombreTienda,
            String fotoTienda,
            String estado,
            String tipoEntrega,
            String direccionEntrega,
            Double total,
            String fecha,
            String fechaActualizacion,
            List<DetalleResumen> detalles
    ) {}

    public record DetalleResumen(
            Long id,
            Integer idVarianteProducto,
            Integer idProducto,
            String nombreProducto,
            String imagenUrl,
            String talla,
            String color,
            String sku,
            Integer cantidad,
            Double precio,
            Long cotizacionId,
            Long personalizacionId
    ) {}
}
