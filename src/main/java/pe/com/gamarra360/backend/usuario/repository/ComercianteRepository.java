package pe.com.gamarra360.backend.usuario.repository;

import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComercianteRepository extends JpaRepository<Comerciante, Integer> {
}
