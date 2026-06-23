package pe.com.gamarra360.backend.solicitud.dto;

/**
 * Detalle completo de una {@code Personalizacion} para la página
 * "Ver detalle" de Mis Personalizaciones.
 */
public record PersonalizacionDetalleResponse(
        Long id,
        String estado,
        String fechaCreacion,
        Integer vendedorId,
        String nombreTienda,
        String fotoTienda,
        Integer detalleProductoId,
        String nombreProducto,
        String imagenUrl,
        String talla,
        String color,
        String sku,
        Integer cantidad,
        String urlLogo,
        String tipoPersonalizacion,
        String descripcion,
        Double precioBase,
        Double descuentos,
        Double costoPersonalizacion,
        Double total,
        PropuestaInfo propuesta,
        PedidoInfo pedido,
        Double precioDeseado
) {
    public record PropuestaInfo(
            Long idRespuesta,
            Double precioPropuesto,
            String comentario,
            String condiciones,
            String anotaciones,
            String imagen,
            String fecha
    ) {}

    public record PedidoInfo(
            Long pedidoId,
            String estado,
            String tipoEntrega,
            String direccionEntrega,
            String fechaActualizacion
    ) {}
}
