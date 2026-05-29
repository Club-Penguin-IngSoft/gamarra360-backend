package pe.com.gamarra360.backend.catalogo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductoResponse {

    private Integer idProducto;
    private String nombre;
    private String descripcion;
    private Double precioBase;
    /**
     * Precio final con el mejor descuento por volumen activo aplicado.
     * Igual a precioBase si no hay descuentos. Null si es PERSONALIZABLE/COTIZACION.
     */
    private Double precioFinal;
    private Boolean esPersonalizable;
    private Boolean activo;
    private Integer idTienda;
    private String nombreTienda;
    private List<CategoriaDto> categorias;
    private TipoProductoDto tipoProducto;
    private List<EspecificacionDto> especificaciones;
    private List<ImagenDto> imagenes;
    private List<VarianteDto> variantes;

    @Getter
    @Setter
    public static class CategoriaDto {
        private Integer idCategoria;
        private String nombre;
    }

    @Getter
    @Setter
    public static class ImagenDto {
        private Integer idImagen;
        private String url;
        private Boolean esPrincipal;
    }

    @Getter
    @Setter
    public static class TipoProductoDto {
        private Integer idTipoProducto;
        private String nombre;
    }

    @Getter
    @Setter
    public static class EspecificacionDto {
        /** Nombre de la especificación (ej. "Material", "Tejido", "Origen"). */
        private String nombre;
        /** Valor de la especificación (ej. "Algodón", "Punto", "Perú"). */
        private String descripcion;
    }

    @Getter
    @Setter
    public static class VarianteDto {
        private Integer idVariante;
        private String sku;
        private Integer stock;
        private Double precioAjustado;
        private Boolean disponible;
        /** Nombre de la talla (ej. "XS", "S", "M"). Null si la variante no tiene talla. */
        private String talla;
        /** Nombre del color (ej. "Negro", "Blanco"). Null si la variante no tiene color. */
        private String color;
        /** Código hex del color (ej. "#000000"), para swatches visuales. */
        private String colorHex;
    }
}