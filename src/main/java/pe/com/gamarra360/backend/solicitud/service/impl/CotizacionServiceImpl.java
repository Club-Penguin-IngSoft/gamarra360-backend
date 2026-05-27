package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionRepository;
import pe.com.gamarra360.backend.solicitud.service.CotizacionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CotizacionServiceImpl extends AbstractCrudService<Cotizacion, Long> implements CotizacionService {

    public CotizacionServiceImpl(CotizacionRepository repository) {
        super(repository, "Cotizacion");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Cotizacion entidad, Long id) {
        entidad.setId(id);
    }
}
