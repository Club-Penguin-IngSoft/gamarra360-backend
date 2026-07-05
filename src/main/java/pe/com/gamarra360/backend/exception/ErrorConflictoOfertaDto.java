package pe.com.gamarra360.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pe.com.gamarra360.backend.catalogo.dto.ConflictoOfertaDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Igual que ErrorRespuestaDto (CLAUDE.md §5), pero agrega el detalle de las
 * ofertas/productos en conflicto para que el frontend pueda mostrar el aviso
 * de sobrescritura sin adivinar nada.
 */
@Data
@Builder
@AllArgsConstructor
public class ErrorConflictoOfertaDto {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String ruta;
    private List<ConflictoOfertaDto> conflictos;
}
