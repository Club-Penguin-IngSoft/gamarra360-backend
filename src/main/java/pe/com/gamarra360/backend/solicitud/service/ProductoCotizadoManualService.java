package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.ProductoCotizadoManual;
import pe.com.gamarra360.backend.solicitud.repository.ProductoCotizadoManualRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProductoCotizadoManualService {

    private final ProductoCotizadoManualRepository repository;

    public ProductoCotizadoManualService(ProductoCotizadoManualRepository repository) {
        this.repository = repository;
    }

    public List<ProductoCotizadoManual> listar() {
        log.info("Listando ProductoCotizadoManual");
        return repository.findAll();
    }

    public ProductoCotizadoManual obtener(Long id) {
        log.info("Obteniendo ProductoCotizadoManual con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ProductoCotizadoManual no encontrado con id " + id));
    }

    public ProductoCotizadoManual crear(ProductoCotizadoManual entidad) {
        log.info("Creando ProductoCotizadoManual");
        return repository.save(entidad);
    }

    public ProductoCotizadoManual actualizar(Long id, ProductoCotizadoManual entidad) {
        log.info("Actualizando ProductoCotizadoManual con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ProductoCotizadoManual no encontrado con id " + id);
        }
        entidad.setId(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando ProductoCotizadoManual con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("ProductoCotizadoManual no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
