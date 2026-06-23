package pe.com.gamarra360.backend.pago.repository;

import pe.com.gamarra360.backend.pago.entity.CarritoPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoPendienteRepository extends JpaRepository<CarritoPendiente, Long> {
}