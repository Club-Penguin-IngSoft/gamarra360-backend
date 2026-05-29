package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.dto.TipoProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.TipoProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;
import pe.com.gamarra360.backend.catalogo.mapper.TipoProductoMapper;
import pe.com.gamarra360.backend.catalogo.repository.CategoriaRepository;
import pe.com.gamarra360.backend.catalogo.repository.TipoProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.TipoProductoService;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.service.AbstractCrudService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TipoProductoServiceImpl extends AbstractCrudService<TipoProducto, Integer>
        implements TipoProductoService {

    private final TipoProductoRepository tipoProductoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoProductoMapper mapper;

    public TipoProductoServiceImpl(TipoProductoRepository tipoProductoRepository,
                                   CategoriaRepository categoriaRepository,
                                   TipoProductoMapper mapper) {
        super(tipoProductoRepository, "TipoProducto");
        this.tipoProductoRepository = tipoProductoRepository;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(TipoProducto entidad, Integer id) {
        entidad.setIdTipoProducto(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoProductoResponse> listarTodosComoResponse() {
        return tipoProductoRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoProductoResponse> listarPorCategoria(Integer idCategoria) {
        return tipoProductoRepository.findByCategoria_IdCategoria(idCategoria).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoProductoResponse obtenerComoResponse(Integer id) {
        return mapper.toResponse(obtener(id));
    }

    @Override
    public TipoProductoResponse crearTipoProducto(TipoProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada con id " + request.getIdCategoria()));
        TipoProducto tipo = mapper.toEntity(request);
        tipo.setCategoria(categoria);
        return mapper.toResponse(tipoProductoRepository.save(tipo));
    }

    @Override
    public TipoProductoResponse actualizarTipoProducto(Integer id, TipoProductoRequest request) {
        TipoProducto tipo = obtener(id);
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada con id " + request.getIdCategoria()));
        tipo.setNombre(request.getNombre());
        tipo.setCategoria(categoria);
        return mapper.toResponse(tipoProductoRepository.save(tipo));
    }
}