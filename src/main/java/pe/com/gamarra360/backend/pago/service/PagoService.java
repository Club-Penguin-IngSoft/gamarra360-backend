package pe.com.gamarra360.backend.pago.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pago.entity.Pago;
import pe.com.gamarra360.backend.pago.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PagoService {

    private final PagoRepository repository;

    public PagoService(PagoRepository repository) {
        this.repository = repository;
    }

    public List<Pago> listar() {
        log.info("Listando Pago");
        return repository.findAll();
    }

    public Pago obtener(Long id) {
        log.info("Obteniendo Pago con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado con id " + id));
    }

    public Pago crear(Pago entidad) {
        log.info("Creando Pago");
        return repository.save(entidad);
    }

    public Pago actualizar(Long id, Pago entidad) {
        log.info("Actualizando Pago con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Pago no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando Pago con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Pago no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
