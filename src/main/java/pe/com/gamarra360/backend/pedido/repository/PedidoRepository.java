package pe.com.gamarra360.backend.pedido.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOrdenPagoId(Long ordenPagoId);
    @EntityGraph(attributePaths = {
            "ordenPago",
            "vendedor",
            "vendedor.tienda",
            "listaDetalles",
            "listaDetalles.varianteProducto",
            "listaDetalles.varianteProducto.producto",
            "listaDetalles.varianteProducto.color",
            "listaDetalles.varianteProducto.talla"
    })
    List<Pedido> findDistinctByClienteIdOrderByFechaDesc(Integer clienteId);

    @EntityGraph(attributePaths = {
            "ordenPago",
            "vendedor",
            "vendedor.tienda",
            "listaDetalles",
            "listaDetalles.varianteProducto",
            "listaDetalles.varianteProducto.producto",
            "listaDetalles.varianteProducto.color",
            "listaDetalles.varianteProducto.talla"
    })
    Optional<Pedido> findByIdAndClienteId(Long id, Integer clienteId);
    Optional<Pedido> findFirstByClienteIdAndDireccionEntregaIsNotNullOrderByFechaDesc(Integer clienteId);
}
