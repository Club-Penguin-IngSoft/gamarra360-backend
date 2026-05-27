package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.RespuestaSolicitud;
import pe.com.gamarra360.backend.solicitud.repository.RespuestaSolicitudRepository;
import pe.com.gamarra360.backend.solicitud.service.RespuestaSolicitudService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RespuestaSolicitudServiceImpl extends AbstractCrudService<RespuestaSolicitud, Long> implements RespuestaSolicitudService {

    public RespuestaSolicitudServiceImpl(RespuestaSolicitudRepository repository) {
        super(repository, "RespuestaSolicitud");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(RespuestaSolicitud entidad, Long id) {
        entidad.setIdRespuesta(id);
    }
}
