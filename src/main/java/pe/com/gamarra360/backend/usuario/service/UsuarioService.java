package pe.com.gamarra360.backend.usuario.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> listar() {
        log.info("Listando Usuario");
        return repository.findAll();
    }

    public Usuario obtener(Integer id) {
        log.info("Obteniendo Usuario con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    public Usuario crear(Usuario entidad) {
        log.info("Creando Usuario");
        return repository.save(entidad);
    }

    public Usuario actualizar(Integer id, Usuario entidad) {
        log.info("Actualizando Usuario con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id " + id);
        }
        entidad.setUsuarioId(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Usuario con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
