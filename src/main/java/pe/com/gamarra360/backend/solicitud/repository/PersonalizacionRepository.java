package pe.com.gamarra360.backend.solicitud.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import pe.com.gamarra360.backend.solicitud.entity.Personalizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalizacionRepository extends JpaRepository<Personalizacion, Long> {
    @EntityGraph(attributePaths = {
            "vendedor",
            "vendedor.tienda",
            "varianteProducto",
            "varianteProducto.producto",
            "varianteProducto.color",
            "varianteProducto.talla"
    })
    List<Personalizacion> findDistinctByClienteIdOrderByFechaCreacionDesc(Integer clienteId);

    @EntityGraph(attributePaths = {
            "vendedor",
            "vendedor.tienda",
            "varianteProducto",
            "varianteProducto.producto",
            "varianteProducto.color",
            "varianteProducto.talla"
    })
    Optional<Personalizacion> findByIdAndClienteId(Long id, Integer clienteId);


}
