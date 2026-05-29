package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.CategoriaRequest;
import pe.com.gamarra360.backend.catalogo.dto.CategoriaResponse;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.service.CrudService;

import java.util.List;

public interface CategoriaService extends CrudService<Categoria, Integer> {
    List<CategoriaResponse> listarTodosComoResponse();
    CategoriaResponse obtenerComoResponse(Integer id);
    CategoriaResponse crearCategoria(CategoriaRequest request);
    CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request);
}