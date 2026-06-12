package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.RespuestaSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RespuestaSolicitudRepository extends JpaRepository<RespuestaSolicitud, Long> {
    Optional<RespuestaSolicitud> findByIdSolicitud(Long idSolicitud);
}
