package pe.com.gamarra360.backend.usuario.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.gamarra360.backend.usuario.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Cliente c SET c.direccionEntrega = :direccionEntrega WHERE c.usuarioId = :id")
    void actualizarPerfil(
            @Param("id") Integer id,
            @Param("direccionEntrega") String direccionEntrega
    );
}