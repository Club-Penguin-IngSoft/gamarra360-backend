package pe.com.gamarra360.backend.pedido.repository;

import pe.com.gamarra360.backend.pedido.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
}
