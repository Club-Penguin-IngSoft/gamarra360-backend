package pe.com.gamarra360.backend.catalogo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.gamarra360.backend.catalogo.entity.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Integer> {
    @Query("SELECT i FROM ImagenProducto i WHERE i.producto.idProducto = :idProducto")
    List<ImagenProducto> findByIdProducto(@Param("idProducto") Integer idProducto);
}