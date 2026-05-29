package pe.com.gamarra360.backend.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;

import java.util.List;

@Repository
public interface TipoProductoRepository extends JpaRepository<TipoProducto, Integer> {

    /** Todos los tipos que pertenecen a una categoria. */
    List<TipoProducto> findByCategoria_IdCategoria(Integer idCategoria);
}
