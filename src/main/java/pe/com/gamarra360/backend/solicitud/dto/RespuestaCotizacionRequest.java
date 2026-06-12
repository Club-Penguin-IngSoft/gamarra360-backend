package pe.com.gamarra360.backend.solicitud.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespuestaCotizacionRequest {
    private Double precioPropuesto;
    private String comentario;
    private String condiciones;
    private String anotaciones;
    private String imagen;
}
