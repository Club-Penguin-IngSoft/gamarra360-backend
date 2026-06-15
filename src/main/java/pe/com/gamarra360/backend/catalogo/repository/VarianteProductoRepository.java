package pe.com.gamarra360.backend.catalogo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Integer> {
    @Query("SELECT v FROM VarianteProducto v WHERE v.producto.idProducto = :idProducto")
    List<VarianteProducto> findByIdProducto(@Param("idProducto") Integer idProducto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VarianteProducto v WHERE v.idVariante = :id")
    Optional<VarianteProducto> findByIdWithLock(@Param("id") Integer id);
}