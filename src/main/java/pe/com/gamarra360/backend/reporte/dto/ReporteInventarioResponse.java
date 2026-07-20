package pe.com.gamarra360.backend.reporte.dto;

import java.util.List;

public record ReporteInventarioResponse(long unidades, long stockBajo, List<InventarioFila> detalle) {
    public record InventarioFila(Integer productoId, Integer varianteId, String producto, String sku,
                                 String talla, String color, String material, String calidad,
                                 int stock, int stockMinimo, boolean activo) {}
}
