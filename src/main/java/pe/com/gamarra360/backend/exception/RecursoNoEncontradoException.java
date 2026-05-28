package pe.com.gamarra360.backend.exception;

/**
 * Lanzada cuando un recurso solicitado por ID no existe en la BD.
 * El GlobalExceptionHandler la traduce a HTTP 404 (CLAUDE.md §5).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String tipoRecurso, Object id) {
        super(String.format("%s con id %s no fue encontrado.", tipoRecurso, id));
    }
}
