package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    List<Cotizacion> findByClienteIdOrderByFechaCreacionDesc(Integer clienteId);
    List<Cotizacion> findByVendedorIdOrderByFechaCreacionDesc(Integer vendedorId);
}
