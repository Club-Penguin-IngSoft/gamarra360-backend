package pe.com.gamarra360.backend.exception;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        log.error("Recurso no encontrado: {}", ex.getMessage());
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> manejarDatosInvalidos(DatosInvalidosException ex, HttpServletRequest request) {
        log.error("Datos invalidos: {}", ex.getMessage());
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Error de validacion", ex);
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("La informacion ingresada no es valida.");
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos", mensaje, request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAcceso(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Acceso no autorizado: {}", ex.getMessage());
        return construir(HttpStatus.FORBIDDEN, "Acceso no autorizado", "No tienes permisos para realizar esta accion.", request.getRequestURI());
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ErrorResponse> manejarCuentaDesactivada(org.springframework.security.authentication.DisabledException ex, HttpServletRequest request) {
        log.error("Cuenta desactivada: {}", ex.getMessage());
        return construir(HttpStatus.FORBIDDEN, "Cuenta desactivada", "Tu cuenta de comerciante está pendiente de aprobación o ha sido desactivada por el administrador.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ha ocurrido un error inesperado. Por favor, vuelve a intentarlo.", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String error, String mensaje, String ruta) {
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(), status.value(), error, mensaje, ruta));
    }
}
