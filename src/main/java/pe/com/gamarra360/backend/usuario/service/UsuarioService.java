package pe.com.gamarra360.backend.usuario.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.usuario.dto.ActualizarPerfilRequest;
import pe.com.gamarra360.backend.usuario.entity.Usuario;

public interface UsuarioService extends CrudService<Usuario, Integer> {
    void actualizarPerfil(Integer id, ActualizarPerfilRequest request);
}
