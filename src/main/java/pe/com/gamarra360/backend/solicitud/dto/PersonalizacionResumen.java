package pe.com.gamarra360.backend.solicitud.dto;

/**
 * Fila de la lista "Mis Personalizaciones". Una por cada {@code Personalizacion}
 * del cliente autenticado, con los datos de tienda/producto/variante ya resueltos.
 */
public record PersonalizacionResumen(
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
        Double total,
        String pedidoEstado
) {}
