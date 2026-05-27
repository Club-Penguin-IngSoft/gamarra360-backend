package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.Talla;
import pe.com.gamarra360.backend.catalogo.repository.TallaRepository;
import pe.com.gamarra360.backend.catalogo.service.TallaService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TallaServiceImpl extends AbstractCrudService<Talla, Integer> implements TallaService {

    public TallaServiceImpl(TallaRepository repository) {
        super(repository, "Talla");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Talla entidad, Integer id) {
        entidad.setIdTalla(id);
    }
}
