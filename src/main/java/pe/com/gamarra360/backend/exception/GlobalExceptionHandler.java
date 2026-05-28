package pe.com.gamarra360.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 *
 * Convierte cualquier excepción de la aplicación en una respuesta uniforme
 * (ErrorRespuestaDto) sin exponer stack traces al cliente (CLAUDE.md §5).
 *
 * Tabla de mapeo HTTP definida en CLAUDE.md §5.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* ----------------------- 404: Recurso no encontrado ------------------ */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        log.warn("404 en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Recurso no encontrado")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /* ----------------------- 400: Validación de DTOs --------------------- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("400 validación en {}: {}", request.getRequestURI(), detalles);

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Datos inválidos")
                .mensaje(detalles.isEmpty() ? "La información ingresada no es válida." : detalles)
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /* ----------------------- 500: Catch-all ------------------------------ */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDto> manejarGenerico(
            Exception ex,
            HttpServletRequest request) {

        log.error("500 inesperado en {}: ", request.getRequestURI(), ex);

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error interno")
                .mensaje("Ha ocurrido un error inesperado.")
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
