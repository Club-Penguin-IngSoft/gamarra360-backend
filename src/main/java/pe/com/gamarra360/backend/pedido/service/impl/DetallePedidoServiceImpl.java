package pe.com.gamarra360.backend.pedido.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.DetallePedidoResponse;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.pedido.service.DetallePedidoService;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DetallePedidoServiceImpl extends AbstractCrudService<DetallePedido, Long> implements DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;

    public DetallePedidoServiceImpl(DetallePedidoRepository repository) {
        super(repository, "DetallePedido");
        this.detallePedidoRepository = repository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(DetallePedido entidad, Long id) {
        entidad.setId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetallePedidoResponse> listarPorPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId).stream()
                .map(this::toResponse)
                .toList();
    }

    private DetallePedidoResponse toResponse(DetallePedido dp) {
        VarianteProducto v = dp.getVarianteProducto();
        String nombreProducto = v != null && v.getProducto() != null ? v.getProducto().getNombre() : null;
        String talla = v != null && v.getTalla() != null ? v.getTalla().getTalla() : null;
        String color = v != null && v.getColor() != null ? v.getColor().getNombre() : null;
        String sku = v != null ? v.getSku() : null;
        return new DetallePedidoResponse(
                dp.getId(),
                dp.getPedidoId(),
                dp.getIdVarianteProducto(),
                dp.getCantidad(),
                dp.getPrecio(),
                nombreProducto,
                talla,
                color,
                sku
        );
    }
}
