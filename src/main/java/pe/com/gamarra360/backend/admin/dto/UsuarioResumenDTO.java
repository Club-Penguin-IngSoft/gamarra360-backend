package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Vista resumida de usuario para la tabla del listado.
 * Expone solo los campos necesarios para la grilla, minimizando el payload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumenDTO {
    private Integer usuarioId;
    private String nombreCompleto;   // nombres + primerApellido
    private String email;
    private String rol;              // RolEnum.name()
    private boolean activo;
    private LocalDateTime fechaRegistro;
}
