package pe.com.gamarra360.backend.usuario.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.usuario.entity.Notificacion;

import java.util.List;

public interface NotificacionService extends CrudService<Notificacion, Integer> {
    void crearNotificacion(
            Integer usuarioId,
            Integer actorId,
            String mensaje,
            String tipo,
            Long referenciaId,
            String referenciaTipo,
            String estadoReferencia,
            String ruta
    );
    List<Notificacion> obtenerPorUsuarioId(Integer usuarioId);
    List<Notificacion> listarPorUsuario(Integer usuarioId);
}
