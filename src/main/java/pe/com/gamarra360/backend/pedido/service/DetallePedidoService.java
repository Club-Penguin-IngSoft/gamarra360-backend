package pe.com.gamarra360.backend.pedido.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DetallePedidoService {

    private final DetallePedidoRepository repository;

    public DetallePedidoService(DetallePedidoRepository repository) {
        this.repository = repository;
    }

    public List<DetallePedido> listar() {
        log.info("Listando DetallePedido");
        return repository.findAll();
    }

    public DetallePedido obtener(Long id) {
        log.info("Obteniendo DetallePedido con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("DetallePedido no encontrado con id " + id));
    }

    public DetallePedido crear(DetallePedido entidad) {
        log.info("Creando DetallePedido");
        return repository.save(entidad);
    }

    public DetallePedido actualizar(Long id, DetallePedido entidad) {
        log.info("Actualizando DetallePedido con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DetallePedido no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando DetallePedido con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DetallePedido no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
