package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.gamarra360.backend.catalogo.dto.StockResponse;
import pe.com.gamarra360.backend.catalogo.dto.StockUpdateRequest;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.VarianteProductoService;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.service.AbstractCrudService;

@Service
@Slf4j
@Transactional
public class VarianteProductoServiceImpl extends AbstractCrudService<VarianteProducto, Integer>
        implements VarianteProductoService {

    private final VarianteProductoRepository varianteProductoRepository;

    public VarianteProductoServiceImpl(VarianteProductoRepository repository) {
        super(repository, "VarianteProducto");
        this.varianteProductoRepository = repository;
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(VarianteProducto entidad, Integer id) { entidad.setIdVariante(id); }

    @Override
    public StockResponse actualizarStock(Integer idVariante, StockUpdateRequest request) {
        VarianteProducto variante = varianteProductoRepository.findById(idVariante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada con id " + idVariante));
        variante.setStock(request.getStock());
        variante.setDisponible(request.getStock() > 0);
        varianteProductoRepository.save(variante);
        log.info("Stock actualizado para variante {}: stock={}, disponible={}", idVariante, request.getStock(), variante.getDisponible());
        return toStockResponse(variante);
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse consultarStock(Integer idVariante) {
        VarianteProducto variante = varianteProductoRepository.findById(idVariante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada con id " + idVariante));
        return toStockResponse(variante);
    }

    private StockResponse toStockResponse(VarianteProducto v) {
        StockResponse r = new StockResponse();
        r.setIdVariante(v.getIdVariante());
        r.setSku(v.getSku());
        r.setStock(v.getStock());
        r.setDisponible(v.getDisponible());
        r.setIdProducto(v.getProducto() != null ? v.getProducto().getIdProducto() : null);
        return r;
    }

    @Override
    @Transactional
    public void actualizarImagen(Integer idVariante, String imagenUrl) {
        VarianteProducto variante = varianteProductoRepository.findById(idVariante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada con id " + idVariante));
        variante.setImagenUrl(imagenUrl);
        varianteProductoRepository.save(variante);
    }

    @Override
    @Transactional
    public void descontarStock(Integer idVariante, Integer cantidad) {
        VarianteProducto variante = varianteProductoRepository
                .findByIdWithLock(idVariante)  // bloqueo pesimista
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Variante no encontrada: " + idVariante));

        if (variante.getStock() < cantidad) {
            throw new IllegalStateException(
                    "Stock insuficiente para variante " + idVariante +
                            ". Disponible: " + variante.getStock() + ", solicitado: " + cantidad);
        }

        int nuevoStock = variante.getStock() - cantidad;
        variante.setStock(nuevoStock);

        // Si llega a 0, marca como no disponible
        if (nuevoStock <= 0) {
            variante.setDisponible(false);
            variante.setStock(0);
        }

        varianteProductoRepository.save(variante);
        log.info("Stock variante {} descontado: {} → {}", idVariante, variante.getStock() + cantidad, nuevoStock);
    }
}
