package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.entity.DescuentoVolumen;
import pe.com.gamarra360.backend.catalogo.repository.DescuentoVolumenRepository;
import pe.com.gamarra360.backend.catalogo.service.DescuentoVolumenService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DescuentoVolumenServiceImpl extends AbstractCrudService<DescuentoVolumen, Integer> implements DescuentoVolumenService {

    public DescuentoVolumenServiceImpl(DescuentoVolumenRepository repository) {
        super(repository, "DescuentoVolumen");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(DescuentoVolumen entidad, Integer id) {
        entidad.setIdDescuento(id);
    }
}
