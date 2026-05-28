package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Envuelve cualquier listado paginado del catálogo.
 *
 * @param <T> tipo del elemento (normalmente ProductoResponse).
 *
 * Campos:
 *  - contenido      → lista de elementos de la página actual
 *  - paginaActual   → índice 0-based de la página
 *  - totalPaginas   → número total de páginas
 *  - totalElementos → número total de registros en la BD
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginaResponse<T> {
    private List<T> contenido;
    private int     paginaActual;
    private int     totalPaginas;
    private long    totalElementos;
}
