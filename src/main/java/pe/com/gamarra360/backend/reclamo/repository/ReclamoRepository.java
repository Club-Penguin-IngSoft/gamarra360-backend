package pe.com.gamarra360.backend.reclamo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.reclamo.entity.Reclamo;
import java.util.List;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    List<Reclamo> findByClienteIdOrderByFechaCreacionDesc(Integer clienteId);
    List<Reclamo> findByVendedorIdOrderByFechaCreacionDesc(Integer vendedorId);
    List<Reclamo> findAllByOrderByFechaCreacionDesc();
}
