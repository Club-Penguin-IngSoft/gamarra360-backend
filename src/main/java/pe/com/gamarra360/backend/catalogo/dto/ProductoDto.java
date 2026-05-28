package pe.com.gamarra360.backend.catalogo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de Producto que se envía al frontend.
 * Alineado 1:1 con `IProducto` (Frontend/src/types/IProducto.ts).
 *
 * El campo `tipoServicio` se DERIVA en el mapper a partir de `esPersonalizable`
 * (la BD no tiene un enum tipo_servicio; ver CLAUDE.md de IProducto.ts).
 *
 * `precioFinal` también se DERIVA en el mapper aplicando la regla
 * `descuentosVolumen` mínima (la que aplica para cantidad=1 o el menor
 * cantidadMinima). Si no hay regla activa, es igual a `precioBase`.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoDto {

    /** ID en string (alineado con el frontend que usa string). */
    private String id;

    private String titulo;
    private String descripcion;

    private Boolean activo;
    private Boolean esPersonalizable;

    /** ID de la tienda dueña del producto (FK directa `productos.id_tienda`) */
    private String idTienda;

    /** ID del comerciante (usuario_id) — derivado de `tienda.comerciante.usuarioId` */
    private String idComerciante;

    private String nombreTienda;

    /** URLs de imágenes (la principal va primero) */
    private List<String> imagenes;

    /** Categoría visible (enum del frontend: HOMBRE/MUJER/etc.) */
    private String categoria;

    /** Etiqueta derivada: COMPRA_DIRECTA o PERSONALIZABLE */
    private String tipoServicio;

    /** Precio base sin descuentos */
    private Double precioBase;

    /** Precio final con la mejor regla de descuento aplicada (cantidad=1) */
    private Double precioFinal;

    private List<VarianteProductoDto> variantes;
    private List<EspecificacionProductoDto> especificaciones;
    private List<DescuentoVolumenDto> descuentosVolumen;
}
