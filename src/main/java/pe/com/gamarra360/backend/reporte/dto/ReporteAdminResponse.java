package pe.com.gamarra360.backend.reporte.dto;

public record ReporteAdminResponse(double ventasBrutas, double comisiones, double devoluciones,
                                   long pedidosPagados, long pedidosCancelados) {}
