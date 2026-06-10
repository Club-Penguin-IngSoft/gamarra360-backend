package pe.com.gamarra360.backend.usuario.dto.perfil;

public record ClienteProductoResumenDto(
        Integer idProducto,
        Integer idVariante,
        String nombreProducto,
        String imagenUrl,
        String color,
        String talla,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {}
