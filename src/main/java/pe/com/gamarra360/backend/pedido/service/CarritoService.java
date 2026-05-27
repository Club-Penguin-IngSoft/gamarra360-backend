package pe.com.gamarra360.backend.pedido.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.Carrito;
import pe.com.gamarra360.backend.pedido.repository.CarritoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CarritoService {

    private final CarritoRepository repository;

    public CarritoService(CarritoRepository repository) {
        this.repository = repository;
    }

    public List<Carrito> listar() {
        log.info("Listando Carrito");
        return repository.findAll();
    }

    public Carrito obtener(Long id) {
        log.info("Obteniendo Carrito con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado con id " + id));
    }

    public Carrito crear(Carrito entidad) {
        log.info("Creando Carrito");
        return repository.save(entidad);
    }

    public Carrito actualizar(Long id, Carrito entidad) {
        log.info("Actualizando Carrito con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Carrito no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando Carrito con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Carrito no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
