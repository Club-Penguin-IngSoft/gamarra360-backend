package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.Especificacion;
import pe.com.gamarra360.backend.catalogo.repository.EspecificacionRepository;
import pe.com.gamarra360.backend.catalogo.service.EspecificacionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EspecificacionServiceImpl extends AbstractCrudService<Especificacion, Integer> implements EspecificacionService {

    public EspecificacionServiceImpl(EspecificacionRepository repository) {
        super(repository, "Especificacion");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Especificacion entidad, Integer id) {
        entidad.setIdEspecificacion(id);
    }
}
