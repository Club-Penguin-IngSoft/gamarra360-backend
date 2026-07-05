package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Oferta activa vigente que comparte productos con la oferta que se intenta guardar. */
@Data
@AllArgsConstructor
public class ConflictoOfertaDto {
    private Integer idOferta;
    private String tituloOferta;
    private List<ProductoConflictoDto> productosEnConflicto;
}
