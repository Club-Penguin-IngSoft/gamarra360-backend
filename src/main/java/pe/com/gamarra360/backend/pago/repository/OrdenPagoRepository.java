package pe.com.gamarra360.backend.pago.repository;

import org.springframework.data.jpa.repository.Query;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenPagoRepository extends JpaRepository<OrdenPago, Long> {
    List<OrdenPago> findByClienteIdOrderByFechaDesc(Integer clienteId);
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrdenPago o WHERE o.estado = 'PAGADO'")
    Double sumarTotalPagado();
}
