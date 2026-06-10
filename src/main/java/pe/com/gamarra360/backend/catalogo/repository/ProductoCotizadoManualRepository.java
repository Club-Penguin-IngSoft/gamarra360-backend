package pe.com.gamarra360.backend.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.gamarra360.backend.catalogo.entity.ProductoCotizadoManual;
import java.util.Optional;

@Repository("catalogoProductoCotizadoManualRepository")
public interface ProductoCotizadoManualRepository extends JpaRepository<ProductoCotizadoManual, Integer> {

    Optional<ProductoCotizadoManual> findByDetalleCotizacionId(Integer detalleCotizacionId);
}