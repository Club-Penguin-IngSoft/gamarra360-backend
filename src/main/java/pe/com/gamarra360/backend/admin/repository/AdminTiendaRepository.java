package pe.com.gamarra360.backend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;

import java.util.Optional;

/**
 * AdminTiendaRepository
 *
 * Repositorio para operaciones administrativas sobre las Tiendas.
 */
@Repository
public interface AdminTiendaRepository extends JpaRepository<Tienda, Integer> {

    Optional<Tienda> findByIdComerciante(Integer idComerciante);

    @Modifying
    @Query("UPDATE Tienda t SET t.activa = false WHERE t.idComerciante = :comercianteId")
    void desactivarTienda(@Param("comercianteId") Integer comercianteId);

    @Modifying
    @Query("UPDATE Producto p SET p.activo = false WHERE p.idTienda = (SELECT t.idTienda FROM Tienda t WHERE t.idComerciante = :comercianteId)")
    void desactivarProductosDeTienda(@Param("comercianteId") Integer comercianteId);

    @Transactional
    default void desactivarTiendaYProductos(Integer comercianteId) {
        desactivarTienda(comercianteId);
        desactivarProductosDeTienda(comercianteId);
    }
}
