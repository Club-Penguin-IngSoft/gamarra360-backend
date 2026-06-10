package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.enums.TipoTrabajo;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.dto.PersonalizacionRequest;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import pe.com.gamarra360.backend.solicitud.repository.PersonalizacionRepository;
import pe.com.gamarra360.backend.solicitud.service.PersonalizacionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PersonalizacionServiceImpl extends AbstractCrudService<Personalizacion, Long> implements PersonalizacionService {

    private final PersonalizacionRepository personalizacionRepository;

    public PersonalizacionServiceImpl(PersonalizacionRepository repository) {
        super(repository, "Personalizacion");
        this.personalizacionRepository = repository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Personalizacion entidad, Long id) {
        entidad.setId(id);
    }

    @Override
    @Transactional
    public Personalizacion crearSolicitud(PersonalizacionRequest request, Integer clienteId) {
        log.info("Creando solicitud de personalización para cliente {} y variante {}",
                clienteId, request.getDetalleProductoId());

        Personalizacion p = new Personalizacion();
        p.setClienteId(clienteId);
        p.setVendedorId(request.getVendedorId());
        p.setDetalleProductoId(request.getDetalleProductoId());

        if (request.getTipoPersonalizacion() != null) {
            p.setTipoPersonalizacion(TipoTrabajo.valueOf(request.getTipoPersonalizacion()));
        }

        p.setUrlLogo(request.getUrlLogo());
        p.setDescripcion(request.getDescripcion());

        Personalizacion saved = personalizacionRepository.save(p);
        log.info("Solicitud de personalización creada con ID {}", saved.getId());
        return saved;
    }
}
