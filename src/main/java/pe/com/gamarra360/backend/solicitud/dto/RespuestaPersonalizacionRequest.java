package pe.com.gamarra360.backend.solicitud.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespuestaPersonalizacionRequest {
    private String decision;
    private Double precioPropuesto;
    private String anotaciones;
    private String condiciones;
    private String comentario;
}
