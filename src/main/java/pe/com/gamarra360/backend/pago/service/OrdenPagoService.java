package pe.com.gamarra360.backend.pago.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrdenPagoService {

    private final OrdenPagoRepository repository;

    public OrdenPagoService(OrdenPagoRepository repository) {
        this.repository = repository;
    }

    public List<OrdenPago> listar() {
        log.info("Listando OrdenPago");
        return repository.findAll();
    }

    public OrdenPago obtener(Long id) {
        log.info("Obteniendo OrdenPago con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("OrdenPago no encontrado con id " + id));
    }

    public OrdenPago crear(OrdenPago entidad) {
        log.info("Creando OrdenPago");
        return repository.save(entidad);
    }

    public OrdenPago actualizar(Long id, OrdenPago entidad) {
        log.info("Actualizando OrdenPago con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("OrdenPago no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando OrdenPago con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("OrdenPago no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
