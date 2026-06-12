package pe.com.gamarra360.backend.pedido.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.pedido.entity.Pedido;

public interface PedidoService extends CrudService<Pedido, Long> {
    void cancelar(Long id, Integer clienteId);
}
