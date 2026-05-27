package pe.com.gamarra360.backend.solicitud.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.solicitud.entity.DetalleCotizacion;
import pe.com.gamarra360.backend.solicitud.repository.DetalleCotizacionRepository;
import pe.com.gamarra360.backend.solicitud.service.DetalleCotizacionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DetalleCotizacionServiceImpl extends AbstractCrudService<DetalleCotizacion, Integer> implements DetalleCotizacionService {

    public DetalleCotizacionServiceImpl(DetalleCotizacionRepository repository) {
        super(repository, "DetalleCotizacion");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(DetalleCotizacion entidad, Integer id) {
        entidad.setDetalleCotizacionId(id);
    }
}
