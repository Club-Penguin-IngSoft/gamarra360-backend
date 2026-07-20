package pe.com.gamarra360.backend.reporte.dto;

import java.util.List;

public record ReporteVentasResponse(
        double totalVentas, long pedidos, long cancelados, double devoluciones,
        List<VentaFila> detalle
) {
    public record VentaFila(Long pedidoId, String fecha, String estado, String tipoEntrega, double total) {}
}
