package pe.com.gamarra360.backend.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.catalogo.entity.Oferta;

import java.util.List;
import java.util.Optional;

public interface OfertaRepository extends JpaRepository<Oferta, Integer> {
    List<Oferta> findByIdTienda(Integer idTienda);
    Optional<Oferta> findByIdOfertaAndIdTienda(Integer idOferta, Integer idTienda);
}
