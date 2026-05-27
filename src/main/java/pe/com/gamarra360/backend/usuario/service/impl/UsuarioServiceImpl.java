package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.UsuarioRepository;
import pe.com.gamarra360.backend.usuario.service.UsuarioService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsuarioServiceImpl extends AbstractCrudService<Usuario, Integer> implements UsuarioService {

    public UsuarioServiceImpl(UsuarioRepository repository) {
        super(repository, "Usuario");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Usuario entidad, Integer id) {
        entidad.setUsuarioId(id);
    }
}
