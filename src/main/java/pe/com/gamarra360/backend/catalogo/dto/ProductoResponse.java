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
    private Double precioFinal;
    private Boolean esPersonalizable;
    private Boolean activo;
    private Integer idTienda;
    private Integer idComerciante;
    private String nombreTienda;
    private Integer idCategoria;
    private String nombreCategoria;
    private Integer idTipoProducto;
    private String nombreTipoProducto;
    private List<ImagenDto> imagenes;
    private List<VarianteDto> variantes;

    @Getter
    @Setter
    public static class ImagenDto {
        private Integer idImagen;
        private String url;
        private Boolean esPrincipal;
    }

    @Getter
    @Setter
    public static class VarianteDto {
        private Integer idVariante;
        private String sku;
        private Integer stock;
        private Double precioAjustado;
        private Boolean disponible;
        private Integer idTalla;
        private Integer idColor;
        // Nombres resueltos para el frontend (evita un segundo round-trip)
        private String talla;
        private String color;
        private String colorHex;
    }
}