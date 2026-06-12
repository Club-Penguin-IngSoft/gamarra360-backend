package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.ProductoCotizadoManual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoCotizadoManualRepository extends JpaRepository<ProductoCotizadoManual, Long> {
    Optional<ProductoCotizadoManual> findByDetalleCotizacionId(Integer detalleCotizacionId);
}
