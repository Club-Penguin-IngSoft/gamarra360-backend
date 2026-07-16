package pe.com.gamarra360.backend.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoCotizacionDto {

    /** "CATALOGO" para productos del catálogo, "MANUAL" para productos ingresados a mano. */
    @NotBlank(message = "El tipo de producto es obligatorio")
    @Pattern(regexp = "CATALOGO|MANUAL", message = "El tipo de producto no es válido")
    private String tipo;

    /** Requerido cuando tipo = CATALOGO. Id de la variante de producto (VarianteProducto). */
    private Integer idVariante;

    /** Requerido cuando tipo = MANUAL. Nombre descriptivo del producto. */
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    private String nombre;

    /** URL S3 de la imagen de referencia (solo MANUAL). */
    @Size(max = 2048, message = "La URL de la imagen es demasiado larga")
    private String imagenUrl;

    /** Requisitos, materiales, colores, tallas y cualquier detalle del pedido. */
    @NotBlank(message = "Las especificaciones del producto son obligatorias")
    @Size(max = 2500, message = "Las especificaciones no pueden superar los 2500 caracteres")
    private String especificacion;

    /** Cantidad solicitada (default 1 si no se envía). */
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
}
