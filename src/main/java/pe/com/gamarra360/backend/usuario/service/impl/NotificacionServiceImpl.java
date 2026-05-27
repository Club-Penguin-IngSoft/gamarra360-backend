package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.entity.Notificacion;
import pe.com.gamarra360.backend.usuario.repository.NotificacionRepository;
import pe.com.gamarra360.backend.usuario.service.NotificacionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificacionServiceImpl extends AbstractCrudService<Notificacion, Integer> implements NotificacionService {

    public NotificacionServiceImpl(NotificacionRepository repository) {
        super(repository, "Notificacion");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Notificacion entidad, Integer id) {
        entidad.setIdNotificacion(id);
    }
}
