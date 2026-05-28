package pe.com.gamarra360.backend.solicitud.repository;

import pe.com.gamarra360.backend.enums.EstadoSolicitud;
import pe.com.gamarra360.backend.solicitud.entity.CotizacionCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CotizacionCatalogoRepository extends JpaRepository<CotizacionCatalogo, Integer> {

    @Query("SELECT COUNT(cc) > 0 FROM CotizacionCatalogo cc " +
           "WHERE cc.varianteProducto.producto.idProducto = :idProducto AND cc.cotizacion.estado IN :estados")
    boolean existeCotizacionActivaPorProducto(@Param("idProducto") Integer idProducto,
                                              @Param("estados") List<EstadoSolicitud> estados);
}