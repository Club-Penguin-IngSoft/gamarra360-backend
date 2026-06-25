package pe.com.gamarra360.backend.solicitud.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContraPropuestaRequest {
    private Double precioDeseado;
    private String especificacion;
    private String comentario;
}
