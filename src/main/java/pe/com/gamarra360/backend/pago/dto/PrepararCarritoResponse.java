package pe.com.gamarra360.backend.pago.dto;

/** Respuesta de POST /api/v1/pagos/preparar */
public record PrepararCarritoResponse(
        Long carritoPendienteId,
        /** Subtotal de ítems calculado server-side (precios reales de BD con ofertas). */
        Double subtotalItems,
        /** Suma de costos de entrega de todos los grupos (backend-authoritative). */
        Double costoEntregaTotal,
        /** Total final validado por backend = subtotalItems + costoEntregaTotal. */
        Double total
) {}
