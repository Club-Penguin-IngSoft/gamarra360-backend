package pe.com.gamarra360.backend.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.gamarra360.backend.usuario.entity.CodigoVerificacion;

import java.util.Optional;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    /** El código más reciente no usado para ese email */
    Optional<CodigoVerificacion> findTopByEmailOrderByExpiracionDesc(String email);

    /** Invalida todos los códigos anteriores de ese email antes de emitir uno nuevo */
    @Modifying
    @Query("UPDATE CodigoVerificacion c SET c.usado = true WHERE c.email = :email AND c.usado = false")
    void invalidarTodosPorEmail(@Param("email") String email);
}
