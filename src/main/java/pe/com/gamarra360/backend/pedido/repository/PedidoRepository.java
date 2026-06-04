package pe.com.gamarra360.backend.pedido.repository;

import pe.com.gamarra360.backend.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOrdenPagoId(Long ordenPagoId);
}
