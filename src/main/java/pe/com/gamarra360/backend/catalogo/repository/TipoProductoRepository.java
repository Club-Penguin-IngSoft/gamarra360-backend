package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoProductoRepository extends JpaRepository<TipoProducto, Integer> {

    /** Todos los tipos que pertenecen a una categoria. */
    List<TipoProducto> findByCategoria_IdCategoria(Integer idCategoria);
}
