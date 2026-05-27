package pe.com.gamarra360.backend.pago.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import pe.com.gamarra360.backend.pago.service.OrdenPagoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrdenPagoServiceImpl extends AbstractCrudService<OrdenPago, Long> implements OrdenPagoService {

    public OrdenPagoServiceImpl(OrdenPagoRepository repository) {
        super(repository, "OrdenPago");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(OrdenPago entidad, Long id) {
        entidad.setId(id);
    }
}
