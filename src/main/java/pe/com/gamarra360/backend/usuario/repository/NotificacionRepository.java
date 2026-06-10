package pe.com.gamarra360.backend.usuario.repository;

import pe.com.gamarra360.backend.usuario.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    long countByUsuarioIdAndFueleidaFalse(Integer usuarioId);
}
