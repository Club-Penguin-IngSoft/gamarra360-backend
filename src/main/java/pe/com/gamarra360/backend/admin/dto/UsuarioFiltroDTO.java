package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filtros para listar usuarios.
 * Todos los campos son opcionales; null = sin filtro.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioFiltroDTO {
    /** Rol del usuario: CLIENTE | VENDEDOR | ADMIN */
    private String rol;
    /** Estado de la cuenta: true=activa, false=inactiva */
    private Boolean activo;
    /** Búsqueda libre por nombre, apellido o email */
    private String q;
}
