package pe.com.gamarra360.backend.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.gamarra360.backend.usuario.entity.Usuario;

import java.util.Optional;

/**
 * AdminUsuarioRepository
 *
 * Repositorio de solo lectura (+escritura de estado) para el módulo de administración.
 */
@Repository
public interface AdminUsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Búsqueda dinámica con filtros opcionales.
     */
    @Query("""
        SELECT u FROM Usuario u
        WHERE (:rol IS NULL OR u.rol = :rol)
          AND (:activo IS NULL OR u.activo = :activo)
          AND (:q IS NULL OR
               LOWER(u.nombres) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(u.primerApellido) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY u.usuarioId ASC
    """)
    Page<Usuario> buscarConFiltros(
            @Param("rol") String rol,
            @Param("activo") Boolean activo,
            @Param("q") String q,
            Pageable pageable
    );

    /**
     * Carga el usuario junto con su historial de actividad.
     * Como no existen los campos mapeados directamente en Usuario, delegamos al findById de Spring Data.
     */
    default Optional<Usuario> findByIdConHistorial(Integer id) {
        return findById(id);
    }
}
