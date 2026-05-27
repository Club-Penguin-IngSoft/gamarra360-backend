package pe.com.gamarra360.backend.pedido.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.pedido.service.PedidoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PedidoServiceImpl extends AbstractCrudService<Pedido, Long> implements PedidoService {

    public PedidoServiceImpl(PedidoRepository repository) {
        super(repository, "Pedido");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Pedido entidad, Long id) {
        entidad.setId(id);
    }
}
