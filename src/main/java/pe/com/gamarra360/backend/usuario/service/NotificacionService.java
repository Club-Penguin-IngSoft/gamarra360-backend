package pe.com.gamarra360.backend.usuario.service;

import lombok.extern.slf4j.Slf4j;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.usuario.entity.Notificacion;
import pe.com.gamarra360.backend.usuario.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NotificacionService {

    private final NotificacionRepository repository;

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
    }

    public List<Notificacion> listar() {
        log.info("Listando Notificacion");
        return repository.findAll();
    }

    public Notificacion obtener(Integer id) {
        log.info("Obteniendo Notificacion con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificacion no encontrado con id " + id));
    }

    public Notificacion crear(Notificacion entidad) {
        log.info("Creando Notificacion");
        return repository.save(entidad);
    }

    public Notificacion actualizar(Integer id, Notificacion entidad) {
        log.info("Actualizando Notificacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Notificacion no encontrado con id " + id);
        }
        entidad.setIdNotificacion(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando Notificacion con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Notificacion no encontrado con id " + id);
        }
        repository.deleteById(id);
    }
}
