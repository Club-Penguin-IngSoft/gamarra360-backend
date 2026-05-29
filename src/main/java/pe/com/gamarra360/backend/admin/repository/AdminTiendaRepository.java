package pe.com.gamarra360.backend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;

import java.util.Optional;

@Repository
public interface AdminTiendaRepository extends JpaRepository<Tienda, Integer> {

    Optional<Tienda> findByIdComerciante(Integer idComerciante);
}