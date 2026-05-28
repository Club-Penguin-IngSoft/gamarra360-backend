package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Producto.
 *
 * Implementa las queries que respaldan CU-07 y CU-08:
 *  - Catálogo público: solo productos activos de tiendas verificadas
 *  - Detalle por id: incluye variantes, imágenes, especificaciones
 *  - Búsqueda por keyword con LIKE (multi-campo)
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ── Derived queries (usadas por ProductoServiceImpl) ─────────────────────

    List<Producto> findByIdTiendaAndActivoTrue(Integer idTienda);

    List<Producto> findByActivoTrue();

    // ── JPQL queries con filtros de visibilidad (CU-07, RF-20/RF-21) ─────────

    /**
     * Listado del catálogo público:
     *  - producto.activo         = TRUE
     *  - tienda.verificada       = TRUE
     *  - comerciante.verificado  = TRUE
     *  - comerciante.activo      = TRUE
     */
    @Query("""
            SELECT DISTINCT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            LEFT JOIN t.comerciante c
            WHERE p.activo = true
              AND t.verificada = true
              AND c.verificado = true
              AND c.activo = true
            """)
    List<Producto> findCatalogoPublico();

    /**
     * Búsqueda por keyword en nombre, descripción y nombre de tienda.
     */
    @Query("""
            SELECT DISTINCT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            LEFT JOIN t.comerciante c
            WHERE p.activo = true
              AND t.verificada = true
              AND c.verificado = true
              AND c.activo = true
              AND (
                LOWER(p.nombre)             LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descripcion)     LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(t.nombreComercial) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    List<Producto> buscarPorKeyword(@Param("q") String q);

    /**
     * Detalle de un producto con reglas de visibilidad del catálogo público.
     */
    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            LEFT JOIN FETCH t.comerciante c
            WHERE p.idProducto = :id
              AND p.activo = true
              AND t.verificada = true
              AND c.verificado = true
              AND c.activo = true
            """)
    Optional<Producto> findByIdActivo(@Param("id") Integer id);

    /**
     * Productos de una tienda específica con filtros de visibilidad.
     */
    @Query("""
            SELECT DISTINCT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            LEFT JOIN t.comerciante c
            WHERE t.idTienda = :idTienda
              AND p.activo = true
              AND t.verificada = true
              AND c.verificado = true
              AND c.activo = true
            """)
    List<Producto> findByTiendaIdConVisibilidad(@Param("idTienda") Integer idTienda);
}
