package pe.com.gamarra360.backend.pago.repository;

import pe.com.gamarra360.backend.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
}
