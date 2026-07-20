package pe.com.gamarra360.backend.configuracion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.configuracion.entity.ParametroSistema;

public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, String> {
}
