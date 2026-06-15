package pe.com.gamarra360.backend.pago.repository;

import pe.com.gamarra360.backend.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Pago> findByOrdenPagoId(Long ordenPagoId);
}
