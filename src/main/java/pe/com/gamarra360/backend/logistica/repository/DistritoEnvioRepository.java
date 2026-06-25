package pe.com.gamarra360.backend.logistica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.logistica.entity.DistritoEnvio;

import java.util.List;

public interface DistritoEnvioRepository extends JpaRepository<DistritoEnvio, Integer> {
    List<DistritoEnvio> findByActivoTrueOrderByCiudadAscNombreAsc();
}
