package pe.com.gamarra360.backend.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.usuario.entity.Notificacion;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
}
