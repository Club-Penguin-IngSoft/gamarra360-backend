package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalizacionRepository extends JpaRepository<Personalizacion, Long> {
    List<Personalizacion> findByClienteIdOrderByFechaCreacionDesc(Integer clienteId);
}
