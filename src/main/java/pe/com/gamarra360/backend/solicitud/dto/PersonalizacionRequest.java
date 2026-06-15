package pe.com.gamarra360.backend.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload que recibe el endpoint POST /api/v1/personalizaciones.
 * El clienteId se deriva del JWT — el frontend no lo envía.
 */
@Getter
@Setter
public class PersonalizacionRequest {

    /** FK → variante_producto (la variante de color/talla elegida por el cliente) */
    @NotNull(message = "La variante del producto es obligatoria")
    private Integer detalleProductoId;

    /** FK → comerciante (owner de la tienda del producto) */
    @NotNull(message = "El vendedor es obligatorio")
    private Integer vendedorId;

    /**
     * Tipo de trabajo: "ESTAMPADO_DTF", "BORDADO_INDUSTRIAL" o "IMPRESION_TEXTIL".
     * Se parsea al enum {@link pe.com.gamarra360.backend.enums.TipoTrabajo}.
     */
    @NotBlank(message = "El tipo de personalización es obligatorio")
    private String tipoPersonalizacion;

    /** URL de S3 con la imagen del diseño subida por el cliente (null si eligió "Solo Texto") */
    private String urlLogo;

    /**
     * Descripción combinada: posición, medidas, instrucciones adicionales y/o texto personalizado.
     * Generada por el frontend antes de enviar.
     */
    private String descripcion;

    /** Cantidad de unidades solicitadas (default 1 si no se envía). */
    private Integer cantidad;
}
