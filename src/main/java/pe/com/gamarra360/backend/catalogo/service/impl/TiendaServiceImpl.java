package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.service.TiendaService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TiendaServiceImpl extends AbstractCrudService<Tienda, Integer> implements TiendaService {

    public TiendaServiceImpl(TiendaRepository repository) {
        super(repository, "Tienda");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Tienda entidad, Integer id) {
        entidad.setIdTienda(id);
    }
}
