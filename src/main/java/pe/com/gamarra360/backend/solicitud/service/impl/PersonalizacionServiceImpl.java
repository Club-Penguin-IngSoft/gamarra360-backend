package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import pe.com.gamarra360.backend.solicitud.repository.PersonalizacionRepository;
import pe.com.gamarra360.backend.solicitud.service.PersonalizacionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PersonalizacionServiceImpl extends AbstractCrudService<Personalizacion, Long> implements PersonalizacionService {

    public PersonalizacionServiceImpl(PersonalizacionRepository repository) {
        super(repository, "Personalizacion");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Personalizacion entidad, Long id) {
        entidad.setId(id);
    }
}
