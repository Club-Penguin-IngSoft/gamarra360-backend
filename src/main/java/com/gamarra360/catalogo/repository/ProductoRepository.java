package com.gamarra360.catalogo.repository;

import com.gamarra360.catalogo.entity.Producto;
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
 *
 * NOTA SOBRE RELEVANCIA: actualmente la lógica de scoring por relevancia vive
 * en el ProductoServiceImpl (Java in-memory). Cuando el volumen lo justifique,
 * se migrará a un MATCH AGAINST con FULLTEXT INDEX en MySQL.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Listado del catálogo público:
     *  - producto.activo            = TRUE
     *  - tienda.verificada          = TRUE
     *  - comerciante.verificado     = TRUE
     *  - usuario.activo             = TRUE
     *
     * Esto cubre RF-20/RF-21 (exclusión automática de vendedores no aprobados).
     */
    @Query("""
            SELECT DISTINCT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            LEFT JOIN t.comerciante c
            LEFT JOIN c.usuario u
            WHERE p.activo = true
              AND t.verificada = true
              AND c.verificado = true
              AND u.activo = true
            """)
    List<Producto> findCatalogoPublico();

    /**
     * Búsqueda por keyword en nombre, descripción y nombre de tienda.
     * El servicio aplica luego el scoring de relevancia y el ordenamiento.
     *
     * Insensible a mayúsculas (LOWER en ambos lados).
     */
    @Query("""
            SELECT DISTINCT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            LEFT JOIN t.comerciante c
            LEFT JOIN c.usuario u
            WHERE p.activo = true
              AND t.verificada = true
              AND c.verificado = true
              AND u.activo = true
              AND (
                LOWER(p.nombre)            LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descripcion)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(t.nombreComercial) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    List<Producto> buscarPorKeyword(@Param("q") String q);

    /**
     * Detalle de un producto. Trae los datos básicos; las colecciones se
     * cargan lazy y el servicio se encarga de inicializarlas según necesidad.
     */
    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            LEFT JOIN FETCH t.comerciante c
            LEFT JOIN FETCH c.usuario u
            WHERE p.idProducto = :id
              AND p.activo = true
            """)
    Optional<Producto> findByIdActivo(@Param("id") Integer id);

    /**
     * Productos de una tienda específica (usado en perfil de tienda).
     */
    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.tienda t
            WHERE t.idTienda = :idTienda
              AND p.activo = true
            """)
    List<Producto> findByTiendaId(@Param("idTienda") Integer idTienda);
}
