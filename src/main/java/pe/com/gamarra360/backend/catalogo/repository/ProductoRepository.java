package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByIdTiendaAndActivoTrue(Integer idTienda);
    List<Producto> findByActivoTrue();
}