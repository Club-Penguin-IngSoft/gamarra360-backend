package pe.com.gamarra360.backend.solicitud.dto;

/**
 * Detalle de una {@code Personalizacion} para la vista "Responder Solicitud"
 * del comerciante.
 */
public record PersonalizacionComercianteDetalle(
        Long id,
        String estado,
        String fechaCreacion,
        Integer clienteId,
        String nombreCliente,
        String emailCliente,
        Integer totalPedidosCliente,
        String nombreProducto,
        String imagenUrl,
        String talla,
        String color,
        Integer cantidad,
        String urlLogo,
        String tipoPersonalizacion,
        String descripcion,
        PropuestaInfo propuesta
) {
    public record PropuestaInfo(
            Double precioPropuesto,
            String comentario,
            String condiciones,
            String anotaciones,
            String fecha
    ) {}
}
