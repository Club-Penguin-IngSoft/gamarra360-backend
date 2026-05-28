package pe.com.gamarra360.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 *
 * Convierte cualquier excepción de la aplicación en una respuesta uniforme
 * (ErrorRespuestaDto) sin exponer stack traces al cliente (CLAUDE.md §5).
 *
 * Tabla de mapeo HTTP:
 *  - 400 → MethodArgumentNotValidException, DatosInvalidosException
 *  - 403 → AccessDeniedException, DisabledException
 *  - 404 → RecursoNoEncontradoException
 *  - 409 → ConflictoNegocioException
 *  - 500 → Exception (catch-all)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

//    @ExceptionHandler(UsuarioNoRegistradoGoogleException.class)
//    public ResponseEntity<?> handleGoogleNoUser(UsuarioNoRegistradoGoogleException ex) {
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("needsRegistration", true);
//        response.put("email", ex.getEmail());
//
//        return ResponseEntity.ok(response);
//    }

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

    /* ----------------------- 400: Lógica de negocio inválida ------------- */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarDatosInvalidos(
            DatosInvalidosException ex,
            HttpServletRequest request) {

        log.warn("400 datos inválidos en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Datos inválidos")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /* ----------------------- 409: Conflicto de negocio ------------------- */
    @ExceptionHandler(ConflictoNegocioException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarConflicto(
            ConflictoNegocioException ex,
            HttpServletRequest request) {

        log.warn("409 conflicto en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflicto")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /* ----------------------- 403: Acceso no autorizado ------------------- */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarAcceso(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("403 acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Acceso no autorizado")
                .mensaje("No tienes permisos para realizar esta acción.")
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /* ----------------------- 403: Cuenta desactivada --------------------- */
    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarCuentaDesactivada(
            org.springframework.security.authentication.DisabledException ex,
            HttpServletRequest request) {

        log.warn("403 cuenta desactivada en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Cuenta desactivada")
                .mensaje("Tu cuenta de comerciante está pendiente de aprobación o ha sido desactivada por el administrador.")
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
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
