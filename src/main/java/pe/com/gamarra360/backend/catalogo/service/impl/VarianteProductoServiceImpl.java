package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.gamarra360.backend.catalogo.dto.StockResponse;
import pe.com.gamarra360.backend.catalogo.dto.StockUpdateRequest;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
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
    private final TiendaRepository tiendaRepository;

    public VarianteProductoServiceImpl(VarianteProductoRepository repository,
                                       TiendaRepository tiendaRepository) {
        super(repository, "VarianteProducto");
        this.varianteProductoRepository = repository;
        this.tiendaRepository = tiendaRepository;
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(VarianteProducto entidad, Integer id) { entidad.setIdVariante(id); }

    /**
     * Actualización parcial: solo sobreescribe los campos no-nulos del request,
     * preservando producto, color, talla, sku e imagenUrl cuando no vienen en el body.
     */
    @Override
    @Transactional
    public VarianteProducto actualizar(Integer id, VarianteProducto request) {
        // JOIN FETCH de color y talla para que queden inicializados antes de que
        // la sesión cierre (open-in-view=false) y Jackson pueda serializarlos.
        VarianteProducto existente = varianteProductoRepository.findByIdConColorYTalla(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("VarianteProducto no encontrado con id " + id));

        if (request.getPrecioAjustado() != null) existente.setPrecioAjustado(request.getPrecioAjustado());
        if (request.getDisponible()     != null) existente.setDisponible(request.getDisponible());
        if (request.getMinimoStock()    != null) existente.setMinimoStock(request.getMinimoStock());
        if (request.getStock()          != null) existente.setStock(request.getStock());
        if (request.getSku()            != null) existente.setSku(request.getSku());
        if (request.getImagenUrl()      != null) existente.setImagenUrl(request.getImagenUrl());

        return varianteProductoRepository.save(existente);
    }

    @Override
    public StockResponse actualizarStock(Integer idVariante, StockUpdateRequest request) {
        VarianteProducto variante = varianteProductoRepository.findById(idVariante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada con id " + idVariante));
        variante.setStock(request.getStock());
        // Solo fuerza inactiva si el stock llega a 0; no re-activa si stock > 0,
        // porque el comerciante puede haber desactivado manualmente la variante.
        if (request.getStock() <= 0) {
            variante.setDisponible(false);
        }
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

    /**
     * Borrado físico de la variante.
     * Si hay FK activas (detalles_pedido / items_carrito que referencian esta variante),
     * MySQL lanzará una DataIntegrityViolationException que el GlobalExceptionHandler
     * convierte en 409 Conflict con mensaje amigable.
     */
    @Override
    @Transactional
    public void eliminarVariante(Integer idVariante, Integer comercianteId) {
        VarianteProducto variante = varianteProductoRepository.findById(idVariante)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "VarianteProducto no encontrado con id " + idVariante));

        Tienda tienda = tiendaRepository.findByIdComerciante(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró tienda para el comerciante con id " + comercianteId));

        Integer idTiendaVariante = variante.getProducto().getIdTienda();
        if (!tienda.getIdTienda().equals(idTiendaVariante)) {
            throw new AccessDeniedException(
                    "No tienes permiso para eliminar variantes de otra tienda.");
        }

        varianteProductoRepository.delete(variante);
        log.info("Variante {} eliminada físicamente por comerciante {}", idVariante, comercianteId);
    }
}
