package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TiendaRepository extends JpaRepository<Tienda, Integer> {

    @Query("SELECT t FROM Tienda t WHERE t.idTienda = :idTienda AND t.verificada = true")
    Optional<Tienda> findByIdTiendaAndVerificada(@Param("idTienda") Integer idTienda);

    /** Busca la tienda asociada a un comerciante específico. */
    Optional<Tienda> findByIdComerciante(Integer idComerciante);

    /**
     * Obtiene todas las tiendas que están verificadas (aprobadas).
     */
    List<Tienda> findAllByVerificadaTrue();
}
