package pe.com.gamarra360.backend.catalogo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.gamarra360.backend.catalogo.dto.StockResponse;
import pe.com.gamarra360.backend.catalogo.dto.StockUpdateRequest;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;

import java.util.List;

@Service
@Slf4j
@Transactional
public class VarianteProductoService {

    private final VarianteProductoRepository repository;

    public VarianteProductoService(VarianteProductoRepository repository) {
        this.repository = repository;
    }

    public List<VarianteProducto> listar() {
        log.info("Listando VarianteProducto");
        return repository.findAll();
    }

    public VarianteProducto obtener(Integer id) {
        log.info("Obteniendo VarianteProducto con id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("VarianteProducto no encontrado con id " + id));
    }

    public VarianteProducto crear(VarianteProducto entidad) {
        log.info("Creando VarianteProducto");
        return repository.save(entidad);
    }

    public VarianteProducto actualizar(Integer id, VarianteProducto entidad) {
        log.info("Actualizando VarianteProducto con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("VarianteProducto no encontrado con id " + id);
        }
        entidad.setIdVariante(id);
        return repository.save(entidad);
    }

    public void eliminar(Integer id) {
        log.info("Eliminando VarianteProducto con id {}", id);
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("VarianteProducto no encontrado con id " + id);
        }
        repository.deleteById(id);
    }

    public StockResponse actualizarStock(Integer idVariante, StockUpdateRequest request) {
        VarianteProducto variante = repository.findById(idVariante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada con id " + idVariante));
        variante.setStock(request.getStock());
        variante.setDisponible(request.getStock() > 0);
        repository.save(variante);
        log.info("Stock actualizado para variante {}: stock={}, disponible={}", idVariante, request.getStock(), variante.getDisponible());
        return toStockResponse(variante);
    }

    @Transactional(readOnly = true)
    public StockResponse consultarStock(Integer idVariante) {
        VarianteProducto variante = repository.findById(idVariante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada con id " + idVariante));
        return toStockResponse(variante);
    }

    private StockResponse toStockResponse(VarianteProducto v) {
        StockResponse r = new StockResponse();
        r.setIdVariante(v.getIdVariante());
        r.setSku(v.getSku());
        r.setStock(v.getStock());
        r.setDisponible(v.getDisponible());
        r.setIdProducto(v.getIdProducto());
        return r;
    }
}
