package pe.com.gamarra360.backend.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pe.com.gamarra360.backend.enums.TipoDescuento;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OfertaRequestDto {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "El tipo de descuento es obligatorio")
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor de descuento es obligatorio")
    @Min(value = 0, message = "El valor de descuento no puede ser negativo")
    private Double valorDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    private Boolean activa = true;

    private List<Integer> idsProductos;
}
