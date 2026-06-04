package pe.com.gamarra360.backend.pedido.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.DetallePedidoResponse;

import java.util.List;

public interface DetallePedidoService extends CrudService<DetallePedido, Long> {
    List<DetallePedidoResponse> listarPorPedido(Long pedidoId);
}
