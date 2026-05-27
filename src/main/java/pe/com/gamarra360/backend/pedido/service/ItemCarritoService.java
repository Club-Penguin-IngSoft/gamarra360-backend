package pe.com.gamarra360.backend.pedido.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.ItemCarrito;
import pe.com.gamarra360.backend.pedido.repository.ItemCarritoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemCarritoService {

    private final ItemCarritoRepository repository;

    public ItemCarritoService(ItemCarritoRepository repository) {
        this.repository = repository;
    }

    public List<ItemCarrito> listar() {
        log.info("Listando ItemCarrito");
        return repository.findAll();
    }

    public ItemCarrito obtener(Long id) {
        log.info("Obteniendo ItemCarrito con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ItemCarrito no encontrado con id " + id));
    }

    public ItemCarrito crear(ItemCarrito entidad) {
        log.info("Creando ItemCarrito");
        return repository.save(entidad);
    }

    public ItemCarrito actualizar(Long id, ItemCarrito entidad) {
        log.info("Actualizando ItemCarrito con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ItemCarrito no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando ItemCarrito con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ItemCarrito no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
