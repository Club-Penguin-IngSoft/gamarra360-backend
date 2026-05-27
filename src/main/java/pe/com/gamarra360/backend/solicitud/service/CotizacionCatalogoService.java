package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.CotizacionCatalogo;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionCatalogoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CotizacionCatalogoService {

    private final CotizacionCatalogoRepository repository;

    public CotizacionCatalogoService(CotizacionCatalogoRepository repository) {
        this.repository = repository;
    }

    public List<CotizacionCatalogo> listar() {
        log.info("Listando CotizacionCatalogo");
        return repository.findAll();
    }

    public CotizacionCatalogo obtener(Integer id) {
        log.info("Obteniendo CotizacionCatalogo con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CotizacionCatalogo no encontrado con id " + id));
    }

    public CotizacionCatalogo crear(CotizacionCatalogo entidad) {
        log.info("Creando CotizacionCatalogo");
        return repository.save(entidad);
    }

    public CotizacionCatalogo actualizar(Integer id, CotizacionCatalogo entidad) {
        log.info("Actualizando CotizacionCatalogo con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("CotizacionCatalogo no encontrado con id " + id);
        }
        entidad.setDetalleCotizacionId(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando CotizacionCatalogo con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("CotizacionCatalogo no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
