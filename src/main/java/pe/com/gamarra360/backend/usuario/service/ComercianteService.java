package pe.com.gamarra360.backend.usuario.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ComercianteService {

    private final ComercianteRepository repository;

    public ComercianteService(ComercianteRepository repository) {
        this.repository = repository;
    }

    public List<Comerciante> listar() {
        log.info("Listando Comerciante");
        return repository.findAll();
    }

    public Comerciante obtener(Integer id) {
        log.info("Obteniendo Comerciante con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado con id " + id));
    }

    public Comerciante crear(Comerciante entidad) {
        log.info("Creando Comerciante");
        return repository.save(entidad);
    }

    public Comerciante actualizar(Integer id, Comerciante entidad) {
        log.info("Actualizando Comerciante con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Comerciante no encontrado con id " + id);
        }
        entidad.setUsuarioId(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Comerciante con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Comerciante no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
