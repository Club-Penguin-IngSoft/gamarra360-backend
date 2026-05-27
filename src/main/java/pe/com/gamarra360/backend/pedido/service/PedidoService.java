package pe.com.gamarra360.backend.pedido.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PedidoService {

    private final PedidoRepository repository;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
    }

    public List<Pedido> listar() {
        log.info("Listando Pedido");
        return repository.findAll();
    }

    public Pedido obtener(Long id) {
        log.info("Obteniendo Pedido con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));
    }

    public Pedido crear(Pedido entidad) {
        log.info("Creando Pedido");
        return repository.save(entidad);
    }

    public Pedido actualizar(Long id, Pedido entidad) {
        log.info("Actualizando Pedido con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Pedido no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando Pedido con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Pedido no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
