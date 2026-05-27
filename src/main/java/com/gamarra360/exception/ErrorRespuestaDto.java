package com.gamarra360.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Estructura uniforme de respuesta de error que devuelve el GlobalExceptionHandler.
 * Formato consumido por el frontend (interceptor de Axios en apiClient.ts):
 *
 *   {
 *     "timestamp": "2026-05-26T14:00:00",
 *     "status":    404,
 *     "error":     "Recurso no encontrado",
 *     "mensaje":   "Producto con id 99 no fue encontrado.",
 *     "ruta":      "/api/v1/productos/99"
 *   }
 *
 * Definido en CLAUDE.md §5.
 */
@Data
@Builder
@AllArgsConstructor
public class ErrorRespuestaDto {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String ruta;
}
