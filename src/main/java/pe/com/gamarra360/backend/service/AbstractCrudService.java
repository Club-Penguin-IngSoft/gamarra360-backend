package pe.com.gamarra360.backend.service;

import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class AbstractCrudService<T, ID> implements CrudService<T, ID> {
    private final JpaRepository<T, ID> repository;
    private final String nombreRecurso;

    protected AbstractCrudService(JpaRepository<T, ID> repository, String nombreRecurso) {
        this.repository = repository;
        this.nombreRecurso = nombreRecurso;
    }

    protected abstract Logger getLog();
    protected abstract void asignarId(T entidad, ID id);

    @Override
    public List<T> listar() {
        getLog().info("Listando {}", nombreRecurso);
        return repository.findAll();
    }

    @Override
    public T obtener(ID id) {
        getLog().info("Obteniendo {} con id {}", nombreRecurso, id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(nombreRecurso + " no encontrado con id " + id));
    }

    @Override
    public T crear(T entidad) {
        getLog().info("Creando {}", nombreRecurso);
        return repository.save(entidad);
    }

    @Override
    public T actualizar(ID id, T entidad) {
        getLog().info("Actualizando {} con id {}", nombreRecurso, id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException(nombreRecurso + " no encontrado con id " + id);
        }
        asignarId(entidad, id);
        return repository.save(entidad);
    }

    @Override
    public void eliminar(ID id) {
        getLog().info("Eliminando {} con id {}", nombreRecurso, id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException(nombreRecurso + " no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
