package pe.com.gamarra360.backend.solicitud.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import pe.com.gamarra360.backend.solicitud.entity.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    @EntityGraph(attributePaths = {
            "tienda",
            "vendedor",
            "vendedor.tienda",
            "listaDetallesCotizacion",
            "respuestaSolicitud"
    })
    List<Cotizacion> findDistinctByClienteIdOrderByFechaCreacionDesc(Integer clienteId);

    @EntityGraph(attributePaths = {
            "tienda",
            "vendedor",
            "vendedor.tienda",
            "listaDetallesCotizacion",
            "respuestaSolicitud"
    })
    Optional<Cotizacion> findByIdAndClienteId(Long id, Integer clienteId);


}
