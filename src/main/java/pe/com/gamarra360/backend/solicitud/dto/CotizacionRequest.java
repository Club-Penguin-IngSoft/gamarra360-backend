package pe.com.gamarra360.backend.solicitud.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CotizacionRequest {

    @NotNull(message = "La tienda es obligatoria")
    private Integer idTienda;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<ProductoCotizacionDto> productos;
}
