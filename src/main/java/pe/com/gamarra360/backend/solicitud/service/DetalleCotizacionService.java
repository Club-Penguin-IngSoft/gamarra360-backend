package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.DetalleCotizacion;
import pe.com.gamarra360.backend.solicitud.repository.DetalleCotizacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DetalleCotizacionService {

    private final DetalleCotizacionRepository repository;

    public DetalleCotizacionService(DetalleCotizacionRepository repository) {
        this.repository = repository;
    }

    public List<DetalleCotizacion> listar() {
        log.info("Listando DetalleCotizacion");
        return repository.findAll();
    }

    public DetalleCotizacion obtener(Integer id) {
        log.info("Obteniendo DetalleCotizacion con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("DetalleCotizacion no encontrado con id " + id));
    }

    public DetalleCotizacion crear(DetalleCotizacion entidad) {
        log.info("Creando DetalleCotizacion");
        return repository.save(entidad);
    }

    public DetalleCotizacion actualizar(Integer id, DetalleCotizacion entidad) {
        log.info("Actualizando DetalleCotizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DetalleCotizacion no encontrado con id " + id);
        }
        entidad.setDetalleCotizacionId(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando DetalleCotizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DetalleCotizacion no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
