package pe.com.gamarra360.backend.pedido.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.pedido.entity.ItemCarrito;
import pe.com.gamarra360.backend.pedido.repository.ItemCarritoRepository;
import pe.com.gamarra360.backend.pedido.service.ItemCarritoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ItemCarritoServiceImpl extends AbstractCrudService<ItemCarrito, Long> implements ItemCarritoService {

    public ItemCarritoServiceImpl(ItemCarritoRepository repository) {
        super(repository, "ItemCarrito");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(ItemCarrito entidad, Long id) {
        entidad.setId(id);
    }
}
