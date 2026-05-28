package pe.com.gamarra360.backend.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;

import java.util.Optional;

/**
 * AdminComercianteRepository
 *
 * Acceso a datos para el módulo de aprobación de vendedores (CU-04, RF-14).
 */
@Repository
public interface AdminComercianteRepository extends JpaRepository<Comerciante, Integer> {

    /**
     * Filtra comerciantes por estado.
     * Estado: PENDIENTE_APROBACION | APROBADO | RECHAZADO | SUSPENDIDO
     */
    @Query("SELECT c FROM Comerciante c WHERE c.estado = :estado")
    Page<Comerciante> findByEstado(@Param("estado") String estado, Pageable pageable);

    /** Conteo de solicitudes por estado, para badges del panel. */
    @Query("SELECT COUNT(c) FROM Comerciante c WHERE c.estado = :estado")
    long countByEstado(@Param("estado") String estado);

    /**
     * Detalle con usuario asociado para la pantalla de revisión.
     * Dado que Comerciante hereda de Usuario con JOINED, Hibernate realiza el JOIN implícitamente.
     */
    @Query("SELECT c FROM Comerciante c WHERE c.usuarioId = :id")
    Optional<Comerciante> findByIdConUsuario(@Param("id") Integer id);
}
