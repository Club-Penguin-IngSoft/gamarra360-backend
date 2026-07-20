package pe.com.gamarra360.backend.logistica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.logistica.entity.TarifaEnvioTienda;
import java.util.List;
import java.util.Optional;

public interface TarifaEnvioTiendaRepository extends JpaRepository<TarifaEnvioTienda, Long> {
    List<TarifaEnvioTienda> findByTiendaIdOrderByDistritoId(Integer tiendaId);
    Optional<TarifaEnvioTienda> findByTiendaIdAndDistritoId(Integer tiendaId, Integer distritoId);
}
