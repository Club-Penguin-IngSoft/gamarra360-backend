package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.catalogo.entity.Talla;
import pe.com.gamarra360.backend.catalogo.repository.TallaRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TallaService {

    private final TallaRepository repository;

    public TallaService(TallaRepository repository) {
        this.repository = repository;
    }

    public List<Talla> listar() {
        log.info("Listando Talla");
        return repository.findAll();
    }

    public Talla obtener(Integer id) {
        log.info("Obteniendo Talla con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Talla no encontrado con id " + id));
    }

    public Talla crear(Talla entidad) {
        log.info("Creando Talla");
        return repository.save(entidad);
    }

    public Talla actualizar(Integer id, Talla entidad) {
        log.info("Actualizando Talla con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Talla no encontrado con id " + id);
        }
        entidad.setIdTalla(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Talla con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Talla no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
