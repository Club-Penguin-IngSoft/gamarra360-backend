package pe.com.gamarra360.backend.catalogo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaResponse {
    private Integer idCategoria;
    private String nombreCategoria;
    private String descripcion;
}