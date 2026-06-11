package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
}
