package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import pe.com.gamarra360.backend.solicitud.repository.PersonalizacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PersonalizacionService {

    private final PersonalizacionRepository repository;

    public PersonalizacionService(PersonalizacionRepository repository) {
        this.repository = repository;
    }

    public List<Personalizacion> listar() {
        log.info("Listando Personalizacion");
        return repository.findAll();
    }

    public Personalizacion obtener(Long id) {
        log.info("Obteniendo Personalizacion con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Personalizacion no encontrado con id " + id));
    }

    public Personalizacion crear(Personalizacion entidad) {
        log.info("Creando Personalizacion");
        return repository.save(entidad);
    }

    public Personalizacion actualizar(Long id, Personalizacion entidad) {
        log.info("Actualizando Personalizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Personalizacion no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando Personalizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Personalizacion no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
