package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.RespuestaSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RespuestaSolicitudRepository extends JpaRepository<RespuestaSolicitud, Long> {
    Optional<RespuestaSolicitud> findByIdSolicitud(Long idSolicitud);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RespuestaSolicitud r WHERE r.idRespuesta = :id")
    void eliminarPorId(@Param("id") Long id);
}
