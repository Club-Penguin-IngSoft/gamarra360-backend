package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Integer> {
    List<VarianteProducto> findByIdProducto(Integer idProducto);
}