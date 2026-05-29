package pe.com.gamarra360.backend.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoProductoRequest {

    @NotBlank(message = "El nombre del tipo de producto es obligatorio")
    private String nombre;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;
}