package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.RespuestaSolicitud;
import pe.com.gamarra360.backend.solicitud.repository.RespuestaSolicitudRepository;
import pe.com.gamarra360.backend.solicitud.repository.SolicitudRepository;
import pe.com.gamarra360.backend.solicitud.service.RespuestaSolicitudService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RespuestaSolicitudServiceImpl extends AbstractCrudService<RespuestaSolicitud, Long> implements RespuestaSolicitudService {

    private final SolicitudRepository solicitudRepository;

    public RespuestaSolicitudServiceImpl(RespuestaSolicitudRepository repository, SolicitudRepository solicitudRepository) {
        super(repository, "RespuestaSolicitud");
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(RespuestaSolicitud entidad, Long id) {
        entidad.setIdRespuesta(id);
    }

    @Override
    @Transactional
    public RespuestaSolicitud crear(RespuestaSolicitud entidad) {
        RespuestaSolicitud guardada = super.crear(entidad);
        solicitudRepository.findById(guardada.getIdSolicitud()).ifPresent(solicitud -> {
            solicitud.marcarComoRespondida();
            solicitudRepository.save(solicitud);
        });
        return guardada;
    }
}
