package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.catalogo.entity.Color;
import pe.com.gamarra360.backend.catalogo.repository.ColorRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ColorService {

    private final ColorRepository repository;

    public ColorService(ColorRepository repository) {
        this.repository = repository;
    }

    public List<Color> listar() {
        log.info("Listando Color");
        return repository.findAll();
    }

    public Color obtener(Integer id) {
        log.info("Obteniendo Color con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Color no encontrado con id " + id));
    }

    public Color crear(Color entidad) {
        log.info("Creando Color");
        return repository.save(entidad);
    }

    public Color actualizar(Integer id, Color entidad) {
        log.info("Actualizando Color con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Color no encontrado con id " + id);
        }
        entidad.setIdColor(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Color con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Color no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
