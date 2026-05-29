package pe.com.gamarra360.backend.catalogo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoProductoResponse {
    private Integer idTipoProducto;
    private String nombre;
    private Integer idCategoria;
    private String nombreCategoria;
}