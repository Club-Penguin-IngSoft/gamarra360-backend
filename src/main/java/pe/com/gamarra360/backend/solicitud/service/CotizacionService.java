package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CotizacionService {

    private final CotizacionRepository repository;

    public CotizacionService(CotizacionRepository repository) {
        this.repository = repository;
    }

    public List<Cotizacion> listar() {
        log.info("Listando Cotizacion");
        return repository.findAll();
    }

    public Cotizacion obtener(Long id) {
        log.info("Obteniendo Cotizacion con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cotizacion no encontrado con id " + id));
    }

    public Cotizacion crear(Cotizacion entidad) {
        log.info("Creando Cotizacion");
        return repository.save(entidad);
    }

    public Cotizacion actualizar(Long id, Cotizacion entidad) {
        log.info("Actualizando Cotizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cotizacion no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando Cotizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cotizacion no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
