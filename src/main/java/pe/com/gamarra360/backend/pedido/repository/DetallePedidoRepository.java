package pe.com.gamarra360.backend.pedido.repository;

import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
}
