package pe.com.gamarra360.backend.catalogo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Integer> {
    @Query("SELECT v FROM VarianteProducto v WHERE v.producto.idProducto = :idProducto")
    List<VarianteProducto> findByIdProducto(@Param("idProducto") Integer idProducto);
}