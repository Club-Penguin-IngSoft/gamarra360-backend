package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Integer> {
    List<ImagenProducto> findByIdProducto(Integer idProducto);
}