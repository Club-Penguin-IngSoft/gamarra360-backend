package pe.com.gamarra360.backend.catalogo.mapper;

import org.springframework.stereotype.Component;
import pe.com.gamarra360.backend.catalogo.dto.TipoProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.TipoProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;

@Component
public class TipoProductoMapper {

    public TipoProductoResponse toResponse(TipoProducto entidad) {
        TipoProductoResponse r = new TipoProductoResponse();
        r.setIdTipoProducto(entidad.getIdTipoProducto());
        r.setNombre(entidad.getNombre());
        if (entidad.getCategoria() != null) {
            r.setIdCategoria(entidad.getCategoria().getIdCategoria());
            r.setNombreCategoria(entidad.getCategoria().getNombreCategoria());
        }
        return r;
    }

    public TipoProducto toEntity(TipoProductoRequest request) {
        TipoProducto e = new TipoProducto();
        e.setNombre(request.getNombre());
        // categoria la resuelve el service con CategoriaRepository
        return e;
    }
}