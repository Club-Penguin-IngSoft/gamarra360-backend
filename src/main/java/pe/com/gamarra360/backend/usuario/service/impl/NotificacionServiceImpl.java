package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.entity.Notificacion;
import pe.com.gamarra360.backend.usuario.repository.NotificacionRepository;
import pe.com.gamarra360.backend.usuario.service.NotificacionService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class NotificacionServiceImpl extends AbstractCrudService<Notificacion, Integer> implements NotificacionService {
    public NotificacionServiceImpl(NotificacionRepository repository) {
        super(repository, "Notificacion");
        this.repository = repository;
    }
    @Override
    protected Logger getLog() {
        return log;
    }
    private final NotificacionRepository repository;
    @Override
    protected void asignarId(Notificacion entidad, Integer id) {
        entidad.setIdNotificacion(id);
    }
    @Override
    public List<Notificacion> listarPorUsuario(Integer usuarioId) {
        return repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }
    @Override
    public void crearNotificacion(
            Integer usuarioId,
            Integer actorId,
            String mensaje,
            String tipo,
            Long referenciaId,
            String referenciaTipo,
            String estadoReferencia,
            String ruta
    ) {

        Notificacion n = new Notificacion();

        n.setUsuarioId(usuarioId);
        n.setActorId(actorId);
        n.setMensaje(mensaje);
        n.setTipo(tipo);

        n.setReferenciaId(referenciaId);
        n.setReferenciaTipo(referenciaTipo);

        n.setEstadoReferencia(estadoReferencia);

        n.setRuta(ruta);

        n.setFueleida(false);
        n.setFechaCreacion(LocalDateTime.now());

        // 🔥 CRÍTICO: usar CRUD base
        crear(n);
    }

    @Override
    public List<Notificacion> obtenerPorUsuarioId(Integer usuarioId) {
        return List.of();
    }
//    @Override
//    public List<Notificacion> obtenerPorUsuarioId(Integer usuarioId) {
//        return repository.findByUsuarioId(usuarioId);
//    }
}
