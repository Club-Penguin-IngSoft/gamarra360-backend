package pe.com.gamarra360.backend.pedido.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.service.DetallePedidoService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DetallePedidoServiceImpl extends AbstractCrudService<DetallePedido, Long> implements DetallePedidoService {

    public DetallePedidoServiceImpl(DetallePedidoRepository repository) {
        super(repository, "DetallePedido");
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(DetallePedido entidad, Long id) {
        entidad.setId(id);
    }
}
