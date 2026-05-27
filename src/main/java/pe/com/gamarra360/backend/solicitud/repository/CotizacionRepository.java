package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
}
