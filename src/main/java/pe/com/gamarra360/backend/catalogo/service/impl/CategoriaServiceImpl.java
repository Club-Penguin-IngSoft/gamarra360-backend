package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.dto.CategoriaRequest;
import pe.com.gamarra360.backend.catalogo.dto.CategoriaResponse;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.catalogo.mapper.CategoriaMapper;
import pe.com.gamarra360.backend.catalogo.repository.CategoriaRepository;
import pe.com.gamarra360.backend.catalogo.service.CategoriaService;
import pe.com.gamarra360.backend.service.AbstractCrudService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CategoriaServiceImpl extends AbstractCrudService<Categoria, Integer>
        implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper mapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper mapper) {
        super(categoriaRepository, "Categoria");
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(Categoria entidad, Integer id) {
        entidad.setIdCategoria(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodosComoResponse() {
        return categoriaRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerComoResponse(Integer id) {
        return mapper.toResponse(obtener(id));
    }

    @Override
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        Categoria categoria = mapper.toEntity(request);
        return mapper.toResponse(categoriaRepository.save(categoria));
    }

    @Override
    public CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request) {
        Categoria categoria = obtener(id);
        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setDescripcion(request.getDescripcion());
        return mapper.toResponse(categoriaRepository.save(categoria));
    }
}