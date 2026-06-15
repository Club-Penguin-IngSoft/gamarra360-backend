package pe.com.gamarra360.backend.catalogo.mapper;

import org.springframework.stereotype.Component;
import pe.com.gamarra360.backend.catalogo.dto.CategoriaRequest;
import pe.com.gamarra360.backend.catalogo.dto.CategoriaResponse;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;

@Component
public class CategoriaMapper {

    public CategoriaResponse toResponse(Categoria entidad) {
        CategoriaResponse r = new CategoriaResponse();
        r.setIdCategoria(entidad.getIdCategoria());
        r.setNombreCategoria(entidad.getNombreCategoria());
        r.setDescripcion(entidad.getDescripcion());
        return r;
    }

    public Categoria toEntity(CategoriaRequest request) {
        Categoria e = new Categoria();
        e.setNombreCategoria(request.getNombreCategoria());
        e.setDescripcion(request.getDescripcion());
        return e;
    }
}