package pe.com.gamarra360.backend.usuario.repository;

import pe.com.gamarra360.backend.usuario.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
}
