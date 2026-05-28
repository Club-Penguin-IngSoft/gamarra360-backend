package pe.com.gamarra360.backend.catalogo.repository;

import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para Categoria.
 *
 * Usado en filtros del catálogo (GET /categorias para alimentar el panel
 * de filtros del frontend).
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
