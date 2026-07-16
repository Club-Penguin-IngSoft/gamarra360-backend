package pe.com.gamarra360.backend.usuario.repository;

import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComercianteRepository extends JpaRepository<Comerciante, Integer> {
    List<Comerciante> findByVerificadoFalse();
    Optional<Comerciante> findByStripeAccountId(String stripeAccountId);
    long countByVerificadoFalse();

    long countByVerificadoTrueAndAprobadoTrue();

    long countByVerificadoTrueAndAprobadoFalse();
}
