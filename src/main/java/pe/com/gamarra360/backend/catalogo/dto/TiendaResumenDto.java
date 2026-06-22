package pe.com.gamarra360.backend.catalogo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.gamarra360.backend.enums.GaleriaEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TiendaResumenDto {
    private Integer idTienda;
    private String nombreComercial;
    private String informacion;
    private String foto;
    private Boolean verificada;
    private GaleriaEnum galeria;
    private Boolean ofreceEnvioDomicilio;
    private String piso;
    private String stand;
    private List<String> categorias;
    private List<String> tiposServicio;
    private List<String> tiposProducto;
}