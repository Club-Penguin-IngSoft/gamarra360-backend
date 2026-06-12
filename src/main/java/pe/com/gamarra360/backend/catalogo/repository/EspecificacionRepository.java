package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.Especificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EspecificacionRepository extends JpaRepository<Especificacion, Integer> {

    List<Especificacion> findByIdProducto(Integer idProducto);

    void deleteByIdProducto(Integer idProducto);

    /** Devuelve los valores de Material distintos y no nulos presentes en productos activos, ordenados. */
    @Query("SELECT DISTINCT e.descripcion FROM Especificacion e " +
           "WHERE UPPER(e.nombre) = 'MATERIAL' AND e.descripcion IS NOT NULL " +
           "ORDER BY e.descripcion")
    List<String> findDistinctMateriales();
}
