package pe.com.gamarra360.backend.exception;

import lombok.Getter;
import pe.com.gamarra360.backend.catalogo.dto.ConflictoOfertaDto;

import java.util.List;

/**
 * 409 — uno o más productos de la oferta que se intenta guardar ya pertenecen
 * a otra oferta activa vigente. El comerciante debe confirmar explícitamente
 * (forzarSobrescritura=true) para reemplazar esa asignación anterior.
 */
@Getter
public class OfertaConflictoException extends RuntimeException {
    private final List<ConflictoOfertaDto> conflictos;

    public OfertaConflictoException(String mensaje, List<ConflictoOfertaDto> conflictos) {
        super(mensaje);
        this.conflictos = conflictos;
    }
}
