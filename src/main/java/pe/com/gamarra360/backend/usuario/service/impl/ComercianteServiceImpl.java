package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import pe.com.gamarra360.backend.usuario.service.ComercianteService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ComercianteServiceImpl extends AbstractCrudService<Comerciante, Integer> implements ComercianteService {

    public ComercianteServiceImpl(ComercianteRepository repository) {
        super(repository, "Comerciante");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Comerciante entidad, Integer id) {
        entidad.setUsuarioId(id);
    }
}
