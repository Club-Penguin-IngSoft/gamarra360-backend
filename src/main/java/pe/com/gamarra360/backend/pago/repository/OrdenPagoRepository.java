package pe.com.gamarra360.backend.pago.repository;

import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenPagoRepository extends JpaRepository<OrdenPago, Long> {
    List<OrdenPago> findByClienteIdOrderByFechaDesc(Integer clienteId);
}
