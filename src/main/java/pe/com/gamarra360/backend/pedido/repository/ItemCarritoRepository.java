package pe.com.gamarra360.backend.pedido.repository;

import pe.com.gamarra360.backend.pedido.entity.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
}
