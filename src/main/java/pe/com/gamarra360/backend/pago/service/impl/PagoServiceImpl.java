package pe.com.gamarra360.backend.pago.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.pago.entity.Pago;
import pe.com.gamarra360.backend.pago.repository.PagoRepository;
import pe.com.gamarra360.backend.pago.service.PagoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PagoServiceImpl extends AbstractCrudService<Pago, Long> implements PagoService {

    public PagoServiceImpl(PagoRepository repository) {
        super(repository, "Pago");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Pago entidad, Long id) {
        entidad.setId(id);
    }
}
