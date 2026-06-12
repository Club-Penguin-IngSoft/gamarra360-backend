package pe.com.gamarra360.backend.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoCotizacionDto {

    /** "CATALOGO" para productos del catálogo, "MANUAL" para productos ingresados a mano. */
    @NotBlank(message = "El tipo de producto es obligatorio")
    private String tipo;

    /** Requerido cuando tipo = CATALOGO. Id de la variante de producto (VarianteProducto). */
    private Integer idVariante;

    /** Requerido cuando tipo = MANUAL. Nombre descriptivo del producto. */
    private String nombre;

    /** URL S3 de la imagen de referencia (solo MANUAL). */
    private String imagenUrl;

    /** Requisitos, materiales, colores, tallas y cualquier detalle del pedido. */
    private String especificacion;

    /** Cantidad solicitada (default 1 si no se envía). */
    private Integer cantidad;
}
