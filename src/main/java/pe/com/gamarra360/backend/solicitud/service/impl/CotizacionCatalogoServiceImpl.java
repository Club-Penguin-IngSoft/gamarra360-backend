package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.CotizacionCatalogo;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionCatalogoRepository;
import pe.com.gamarra360.backend.solicitud.service.CotizacionCatalogoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CotizacionCatalogoServiceImpl extends AbstractCrudService<CotizacionCatalogo, Integer> implements CotizacionCatalogoService {

    public CotizacionCatalogoServiceImpl(CotizacionCatalogoRepository repository) {
        super(repository, "CotizacionCatalogo");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(CotizacionCatalogo entidad, Integer id) {
        entidad.setDetalleCotizacionId(id);
    }
}
