package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.catalogo.entity.Especificacion;
import pe.com.gamarra360.backend.catalogo.repository.EspecificacionRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EspecificacionService {

    private final EspecificacionRepository repository;

    public EspecificacionService(EspecificacionRepository repository) {
        this.repository = repository;
    }

    public List<Especificacion> listar() {
        log.info("Listando Especificacion");
        return repository.findAll();
    }

    public Especificacion obtener(Integer id) {
        log.info("Obteniendo Especificacion con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especificacion no encontrado con id " + id));
    }

    public Especificacion crear(Especificacion entidad) {
        log.info("Creando Especificacion");
        return repository.save(entidad);
    }

    public Especificacion actualizar(Integer id, Especificacion entidad) {
        log.info("Actualizando Especificacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Especificacion no encontrado con id " + id);
        }
        entidad.setIdEspecificacion(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Especificacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Especificacion no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
