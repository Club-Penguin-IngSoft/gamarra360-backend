package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TiendaInfoResponse {
    private Integer idTienda;
    private String nombreComercial;
    private String ruc;
}
