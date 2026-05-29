package pe.com.gamarra360.backend.catalogo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TiendaResumenDto {
    private Integer idTienda;
    private String nombreComercial;
    private String informacion;
    private String foto;
    private Boolean verificada; // Para confirmar que están verificadas
    private List<String> categorias;      // NUEVO
    private List<String> tiposServicio;   // NUEVO
}