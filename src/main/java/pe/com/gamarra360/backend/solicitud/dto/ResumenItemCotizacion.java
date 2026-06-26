package pe.com.gamarra360.backend.solicitud.dto;

/**
 * Resumen mínimo del primer producto de una cotización, usado por los módulos
 * {@code pago} y {@code pedido} para mostrar nombre e imagen del ítem en los
 * pedidos generados a partir de una cotización (cuyo DetallePedido no tiene
 * variante propia).
 */
public record ResumenItemCotizacion(String nombre, String imagenUrl) {}
