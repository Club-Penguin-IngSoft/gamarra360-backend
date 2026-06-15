package pe.com.gamarra360.backend.catalogo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripcion es obligatoria")
    private String descripcion;

    @NotNull(message = "El precio base es obligatorio")
    @Positive(message = "El precio base debe ser mayor a cero")
    private Double precioBase;

    private Boolean esPersonalizable = false;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    @NotNull(message = "El tipo de producto es obligatorio")
    private Integer idTipoProducto;

    @NotEmpty(message = "Debe incluir al menos una imagen")
    @Valid
    private List<ImagenRequest> imagenes;

    private List<EspecificacionProductoDto> especificaciones = new ArrayList<>();
}