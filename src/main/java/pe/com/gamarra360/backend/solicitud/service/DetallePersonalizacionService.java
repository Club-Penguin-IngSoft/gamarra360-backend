package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.DetallePersonalizacion;
import pe.com.gamarra360.backend.solicitud.repository.DetallePersonalizacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DetallePersonalizacionService {

    private final DetallePersonalizacionRepository repository;

    public DetallePersonalizacionService(DetallePersonalizacionRepository repository) {
        this.repository = repository;
    }

    public List<DetallePersonalizacion> listar() {
        log.info("Listando DetallePersonalizacion");
        return repository.findAll();
    }

    public DetallePersonalizacion obtener(Integer id) {
        log.info("Obteniendo DetallePersonalizacion con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("DetallePersonalizacion no encontrado con id " + id));
    }

    public DetallePersonalizacion crear(DetallePersonalizacion entidad) {
        log.info("Creando DetallePersonalizacion");
        return repository.save(entidad);
    }

    public DetallePersonalizacion actualizar(Integer id, DetallePersonalizacion entidad) {
        log.info("Actualizando DetallePersonalizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DetallePersonalizacion no encontrado con id " + id);
        }
        entidad.setIdDetallePersonalizacion(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando DetallePersonalizacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("DetallePersonalizacion no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
