package pe.com.gamarra360.backend.solicitud.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.solicitud.entity.RespuestaSolicitud;
import pe.com.gamarra360.backend.solicitud.repository.RespuestaSolicitudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RespuestaSolicitudService {

    private final RespuestaSolicitudRepository repository;

    public RespuestaSolicitudService(RespuestaSolicitudRepository repository) {
        this.repository = repository;
    }

    public List<RespuestaSolicitud> listar() {
        log.info("Listando RespuestaSolicitud");
        return repository.findAll();
    }

    public RespuestaSolicitud obtener(Long id) {
        log.info("Obteniendo RespuestaSolicitud con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("RespuestaSolicitud no encontrado con id " + id));
    }

    public RespuestaSolicitud crear(RespuestaSolicitud entidad) {
        log.info("Creando RespuestaSolicitud");
        return repository.save(entidad);
    }

    public RespuestaSolicitud actualizar(Long id, RespuestaSolicitud entidad) {
        log.info("Actualizando RespuestaSolicitud con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("RespuestaSolicitud no encontrado con id " + id);
        }
        entidad.setIdRespuesta(id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        log.info("Eliminando RespuestaSolicitud con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("RespuestaSolicitud no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
