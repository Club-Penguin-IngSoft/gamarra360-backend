package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilTiendaPublicaDto {
    private Integer idTienda;
    private String nombreComercial;
    private String informacion;
    private String foto;
    private Boolean verificada;
    private List<String> categorias;      // Categorías que vende la tienda (para el hero)
    private List<String> tiposServicio;   // Decide qué secciones mostrar (catálogo / cotización)
    private List<String> tiposProducto;   // "Especialistas en..." (para CotizacionSection)
    private List<ProductoResumenDto> productos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoResumenDto {
        private Integer idProducto;
        private String nombre;
        private String descripcion;
        private Double precioBase;
        private String foto; // Primera imagen si existe
    }
}
