package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.catalogo.repository.CategoriaRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<Categoria> listar() {
        log.info("Listando Categoria");
        return repository.findAll();
    }

    public Categoria obtener(Integer id) {
        log.info("Obteniendo Categoria con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrado con id " + id));
    }

    public Categoria crear(Categoria entidad) {
        log.info("Creando Categoria");
        return repository.save(entidad);
    }

    public Categoria actualizar(Integer id, Categoria entidad) {
        log.info("Actualizando Categoria con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Categoria no encontrado con id " + id);
        }
        entidad.setIdCategoria(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Categoria con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Categoria no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}