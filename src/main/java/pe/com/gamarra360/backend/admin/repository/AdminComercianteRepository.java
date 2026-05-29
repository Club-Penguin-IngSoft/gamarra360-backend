package pe.com.gamarra360.backend.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;

import java.util.Optional;

@Repository
public interface AdminComercianteRepository extends JpaRepository<Comerciante, Integer> {

    // Pendientes: verificado = false
    Page<Comerciante> findByVerificado(Boolean verificado, Pageable pageable);

    // Filtro por verificado + aprobado (reemplaza el activo que no existe en comerciantes)
    Page<Comerciante> findByVerificadoAndAprobado(Boolean verificado, Boolean aprobado, Pageable pageable);

    @Query("SELECT c FROM Comerciante c WHERE c.usuarioId = :id")
    Optional<Comerciante> findByIdConUsuario(@Param("id") Integer id);
}