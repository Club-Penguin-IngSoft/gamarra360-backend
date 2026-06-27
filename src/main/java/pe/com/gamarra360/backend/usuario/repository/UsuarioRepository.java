package pe.com.gamarra360.backend.usuario.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findTop10ByOrderByUsuarioIdDesc();

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.nombres = :nombres, u.primerApellido = :primerApellido, u.segundoApellido = :segundoApellido, u.telefono = :telefono WHERE u.usuarioId = :id")
    void actualizarPerfil(
            @Param("id") Integer id,
            @Param("nombres") String nombres,
            @Param("primerApellido") String primerApellido,
            @Param("segundoApellido") String segundoApellido,
            @Param("telefono") String telefono
    );
}
