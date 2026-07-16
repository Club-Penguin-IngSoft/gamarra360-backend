package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pe.com.gamarra360.backend.enums.TipoDescuento;

@Getter
@Setter
@AllArgsConstructor
public class OfertaResumenDto {
    private String titulo;
    private TipoDescuento tipoDescuento;
    private Double valorDescuento;
}
