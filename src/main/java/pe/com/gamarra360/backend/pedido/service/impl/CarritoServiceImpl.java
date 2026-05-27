package pe.com.gamarra360.backend.pedido.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.pedido.entity.Carrito;
import pe.com.gamarra360.backend.pedido.repository.CarritoRepository;
import pe.com.gamarra360.backend.pedido.service.CarritoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CarritoServiceImpl extends AbstractCrudService<Carrito, Long> implements CarritoService {

    public CarritoServiceImpl(CarritoRepository repository) {
        super(repository, "Carrito");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Carrito entidad, Long id) {
        entidad.setId(id);
    }
}
