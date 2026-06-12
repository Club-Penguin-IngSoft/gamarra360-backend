package pe.com.gamarra360.backend.solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CotizacionResumen {
    private Long id;
    private String estado;
    private String fechaCreacion;
    private Integer idTienda;
    private String nombreTienda;
    private String fotoTienda;
    private Integer cantidadProductos;
    private Double precioPropuesto;
    private String nombreCliente;
}
