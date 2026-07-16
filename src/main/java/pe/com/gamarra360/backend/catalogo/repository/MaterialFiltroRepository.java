package pe.com.gamarra360.backend.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.gamarra360.backend.catalogo.entity.MaterialFiltro;

@Repository
public interface MaterialFiltroRepository extends JpaRepository<MaterialFiltro, Integer> {
}
