package pe.com.gamarra360.backend.pedido.repository;

import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoId(Long pedidoId);

    @Query("SELECT COUNT(dp) > 0 FROM DetallePedido dp " +
           "JOIN dp.pedido p " +
           "JOIN dp.varianteProducto vp " +
           "WHERE vp.producto.idProducto = :idProducto AND p.estado IN :estados")
    boolean existePedidoActivoPorProducto(@Param("idProducto") Integer idProducto,
                                          @Param("estados") List<EstadoPedido> estados);
}