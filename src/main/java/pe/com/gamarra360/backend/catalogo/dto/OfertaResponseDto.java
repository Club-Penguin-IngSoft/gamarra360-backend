package pe.com.gamarra360.backend.catalogo.dto;

import lombok.Getter;
import lombok.Setter;
import pe.com.gamarra360.backend.enums.TipoDescuento;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OfertaResponseDto {

    private Integer idOferta;
    private Integer idTienda;
    private String titulo;
    private String descripcion;
    private TipoDescuento tipoDescuento;
    private Double valorDescuento;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activa;

    /** Calculado dinámicamente: PAUSADO | PROGRAMADO | ACTIVO | FINALIZADO */
    private String estado;

    private List<Integer> idsProductos;
}
