package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Respuesta a operaciones de activación/desactivación de cuenta. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEstadoDTO {
    private Integer usuarioId;
    private boolean activo;
    private String mensaje;
}
