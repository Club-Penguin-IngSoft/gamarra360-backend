package pe.com.gamarra360.backend.solicitud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.solicitud.entity.MensajePersonalizacion;
import java.util.List;

public interface MensajePersonalizacionRepository extends JpaRepository<MensajePersonalizacion, Long> {
    List<MensajePersonalizacion> findByPersonalizacionIdOrderByFechaAsc(Long personalizacionId);
}
