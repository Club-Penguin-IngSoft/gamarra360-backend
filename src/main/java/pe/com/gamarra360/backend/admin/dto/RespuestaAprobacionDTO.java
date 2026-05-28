package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta genérica a operaciones de cambio de estado del vendedor.
 * Usada para: aprobar, rechazar, suspender.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaAprobacionDTO {
    private Integer comercianteId;
    private String nuevoEstado;
    private String mensaje;
}
