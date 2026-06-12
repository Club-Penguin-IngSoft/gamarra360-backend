package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.DetalleCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Integer> {
    List<DetalleCotizacion> findByIdCotizacion(Long idCotizacion);
    int countByIdCotizacion(Long idCotizacion);
}
