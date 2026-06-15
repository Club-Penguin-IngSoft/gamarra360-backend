package pe.com.gamarra360.backend.pedido.entity;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * DTO que recibe el frontend para crear un pedido con pago simulado.
 * El clienteId se extrae del JWT en el controller — no viene en el body.
 */
public record PedidoRequestDTO(

        @NotNull(message = "El vendedor es obligatorio")
        Integer vendedorId,

        @NotNull(message = "El tipo de entrega es obligatorio")
        String tipoEntrega,           // "DELIVERY" | "RECOJO_TIENDA"

        String direccionEntrega,      // requerido solo si tipoEntrega = DELIVERY

        @NotNull @Positive
        Double total,

        @NotEmpty(message = "El pedido debe tener al menos un ítem")
        @Valid
        List<ItemDTO> items

) {
    public record ItemDTO(
            @NotNull Integer idVarianteProducto,
            @NotNull @Positive Integer cantidad,
            @NotNull @Positive Double precio
    ) {}
}
