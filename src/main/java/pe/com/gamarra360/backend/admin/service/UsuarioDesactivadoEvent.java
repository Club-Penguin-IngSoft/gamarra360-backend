package pe.com.gamarra360.backend.admin.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.gamarra360.backend.usuario.entity.Usuario;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDesactivadoEvent {
    private Usuario usuario;
    private String razon;
}
