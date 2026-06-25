package pe.com.gamarra360.backend.pedido.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;

import java.util.List;
import java.util.Optional;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoId(Long pedidoId);

    Optional<DetallePedido> findByPersonalizacionId(Long personalizacionId);

    @Query("SELECT COUNT(dp) > 0 FROM DetallePedido dp " +
           "JOIN dp.pedido p " +
           "JOIN dp.varianteProducto vp " +
           "WHERE vp.producto.idProducto = :idProducto AND p.estado IN :estados")
    boolean existePedidoActivoPorProducto(@Param("idProducto") Integer idProducto,
                                          @Param("estados") List<EstadoPedido> estados);

    /** Detalles de todos los pedidos de un vendedor (para calcular top productos). */
    @Query("SELECT dp FROM DetallePedido dp " +
           "JOIN dp.pedido p " +
           "WHERE p.vendedorId = :vendedorId AND p.id IN :pedidoIds")
    List<DetallePedido> findByVendedorPedidos(
            @Param("vendedorId") Integer vendedorId,
            @Param("pedidoIds") List<Long> pedidoIds
    );
}
