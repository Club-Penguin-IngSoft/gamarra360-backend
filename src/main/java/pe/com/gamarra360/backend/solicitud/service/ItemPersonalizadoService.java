package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.ItemPersonalizado;
import pe.com.gamarra360.backend.solicitud.repository.ItemPersonalizadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemPersonalizadoService {

    private final ItemPersonalizadoRepository repository;

    public ItemPersonalizadoService(ItemPersonalizadoRepository repository) {
        this.repository = repository;
    }

    public List<ItemPersonalizado> listar() {
        log.info("Listando ItemPersonalizado");
        return repository.findAll();
    }

    public ItemPersonalizado obtener(Long id) {
        log.info("Obteniendo ItemPersonalizado con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ItemPersonalizado no encontrado con id " + id));
    }

    public ItemPersonalizado crear(ItemPersonalizado entidad) {
        log.info("Creando ItemPersonalizado");
        return repository.save(entidad);
    }

    public ItemPersonalizado actualizar(Long id, ItemPersonalizado entidad) {
        log.info("Actualizando ItemPersonalizado con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ItemPersonalizado no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando ItemPersonalizado con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ItemPersonalizado no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
