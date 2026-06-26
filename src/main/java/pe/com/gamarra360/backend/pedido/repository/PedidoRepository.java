package pe.com.gamarra360.backend.pedido.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.pedido.entity.Pedido;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOrdenPagoId(Long ordenPagoId);

    List<Pedido> findByVendedorIdOrderByFechaDesc(Integer vendedorId);

    List<Pedido> findByClienteIdAndVendedorIdOrderByFechaDesc(Integer clienteId, Integer vendedorId);

    /** Pedidos del vendedor en un rango de fechas, ordenados de más reciente a más antiguo. */
    List<Pedido> findByVendedorIdAndFechaBetweenOrderByFechaDesc(
            Integer vendedorId,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
