package pe.com.gamarra360.backend.pedido.dto;

import java.util.List;

/**
 * DTO de respuesta para el dashboard del comerciante.
 * Devuelve métricas filtradas por rango de fechas.
 */
public record DashboardResumenDTO(
        List<PedidoPorDia>       pedidosPorDia,
        List<ProductoTop>        productosTopSolicitados,
        List<ProductoTop>        todosLosProductos,
        List<PedidoComercianteResumen> pedidosRecientes,
        List<PedidoComercianteResumen> pedidosCompletados,
        int                      totalUnidades,
        double                   totalIngresos
) {

    /** Cantidad de pedidos agrupados por fecha (YYYY-MM-DD). */
    public record PedidoPorDia(String fecha, int cantidad) {}

    /** Producto más solicitado con cantidad total de unidades vendidas. */
    public record ProductoTop(
            Integer idProducto,
            String  nombreProducto,
            String  imagenUrl,
            int     unidades
    ) {}
}
