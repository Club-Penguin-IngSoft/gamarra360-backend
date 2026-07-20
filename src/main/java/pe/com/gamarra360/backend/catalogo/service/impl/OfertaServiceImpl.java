package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.dto.ConflictoOfertaDto;
import pe.com.gamarra360.backend.catalogo.dto.OfertaRequestDto;
import pe.com.gamarra360.backend.catalogo.dto.OfertaResponseDto;
import pe.com.gamarra360.backend.catalogo.dto.ProductoConflictoDto;
import pe.com.gamarra360.backend.catalogo.entity.Oferta;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.repository.OfertaRepository;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.service.OfertaService;
import pe.com.gamarra360.backend.enums.TipoDescuento;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.OfertaConflictoException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OfertaServiceImpl implements OfertaService {

    private final OfertaRepository ofertaRepository;
    private final ProductoRepository productoRepository;
    private final TiendaRepository tiendaRepository;

    // ── CRUD tenantizado ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponseDto> listar(Integer comercianteId) {
        Integer idTienda = resolverIdTienda(comercianteId);
        log.info("Listando ofertas de tienda {}", idTienda);
        return ofertaRepository.findByIdTienda(idTienda).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OfertaResponseDto obtener(Integer idOferta, Integer comercianteId) {
        Integer idTienda = resolverIdTienda(comercianteId);
        Oferta oferta = ofertaRepository.findByIdOfertaAndIdTienda(idOferta, idTienda)
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta no encontrada con id " + idOferta));
        return toResponseDto(oferta);
    }

    @Override
    @Transactional
    public OfertaResponseDto crear(OfertaRequestDto request, Integer comercianteId) {
        Integer idTienda = resolverIdTienda(comercianteId);
        validarFechas(request);

        Oferta oferta = new Oferta();
        oferta.setIdTienda(idTienda);
        mapearCampos(oferta, request);
        oferta = ofertaRepository.save(oferta);
        log.info("Oferta creada con id {} para tienda {}", oferta.getIdOferta(), idTienda);

        asignarProductos(oferta, request, idTienda);
        return toResponseDto(oferta);
    }

    @Override
    @Transactional
    public OfertaResponseDto actualizar(Integer idOferta, OfertaRequestDto request, Integer comercianteId) {
        Integer idTienda = resolverIdTienda(comercianteId);
        validarFechas(request);

        Oferta oferta = ofertaRepository.findByIdOfertaAndIdTienda(idOferta, idTienda)
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta no encontrada con id " + idOferta));

        mapearCampos(oferta, request);
        ofertaRepository.save(oferta);
        log.info("Oferta {} actualizada para tienda {}", idOferta, idTienda);

        desasignarProductosActuales(idOferta);
        asignarProductos(oferta, request, idTienda);
        return toResponseDto(oferta);
    }

    @Override
    @Transactional
    public void eliminar(Integer idOferta, Integer comercianteId) {
        Integer idTienda = resolverIdTienda(comercianteId);
        Oferta oferta = ofertaRepository.findByIdOfertaAndIdTienda(idOferta, idTienda)
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta no encontrada con id " + idOferta));
        desasignarProductosActuales(idOferta);
        ofertaRepository.delete(oferta);
        log.info("Oferta {} eliminada de tienda {}", idOferta, idTienda);
    }

    @Override
    @Transactional
    public OfertaResponseDto toggleActiva(Integer idOferta, Integer comercianteId) {
        Integer idTienda = resolverIdTienda(comercianteId);
        Oferta oferta = ofertaRepository.findByIdOfertaAndIdTienda(idOferta, idTienda)
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta no encontrada con id " + idOferta));
        oferta.setActiva(!Boolean.TRUE.equals(oferta.getActiva()));
        ofertaRepository.save(oferta);
        log.info("Oferta {} toggled → activa={} en tienda {}", idOferta, oferta.getActiva(), idTienda);
        return toResponseDto(oferta);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private Integer resolverIdTienda(Integer comercianteId) {
        return tiendaRepository.findByIdComerciante(comercianteId)
                .orElseThrow(() -> new DatosInvalidosException("No tienes una tienda asignada."))
                .getIdTienda();
    }

    private void mapearCampos(Oferta oferta, OfertaRequestDto req) {
        oferta.setTitulo(req.getTitulo());
        oferta.setDescripcion(req.getDescripcion());
        oferta.setTipoDescuento(req.getTipoDescuento());
        oferta.setValorDescuento(req.getValorDescuento());
        oferta.setCantidadMinima(req.getCantidadMinima() != null ? req.getCantidadMinima() : 1);
        oferta.setFechaInicio(req.getFechaInicio().atStartOfDay());
        // Regla 23:59:59: ajustar siempre al último segundo del día
        oferta.setFechaFin(req.getFechaFin().atTime(LocalTime.of(23, 59, 59)));
        oferta.setActiva(req.getActiva() != null ? req.getActiva() : Boolean.TRUE);
    }

    private void validarFechas(OfertaRequestDto req) {
        if (req.getFechaFin().isBefore(req.getFechaInicio())) {
            throw new DatosInvalidosException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    private void asignarProductos(Oferta oferta, OfertaRequestDto req, Integer idTienda) {
        List<Integer> idsProductos = req.getIdsProductos();
        if (idsProductos == null || idsProductos.isEmpty()) {
            throw new DatosInvalidosException("Debes seleccionar al menos un producto para la promoción.");
        }

        // Solo productos publicados pueden llevar promoción (RF: promociones requieren productos publicados).
        List<Producto> productos = productoRepository.findByIdProductoInAndIdTiendaAndActivoTrue(idsProductos, idTienda);
        if (productos.isEmpty()) {
            throw new DatosInvalidosException(
                    "Ninguno de los productos seleccionados está publicado. Publica al menos un producto para poder crear la promoción.");
        }
        if (productos.size() != idsProductos.size()) {
            log.warn("Algunos productos no pertenecen a la tienda {}, no existen o no están publicados — se omiten", idTienda);
        }

        validarDescuentoFijo(req, productos);

        if (!Boolean.TRUE.equals(req.getForzarSobrescritura())) {
            List<ConflictoOfertaDto> conflictos = detectarConflictos(oferta, productos);
            if (!conflictos.isEmpty()) {
                throw new OfertaConflictoException(
                        "Uno o más productos ya tienen otra oferta activa vigente.", conflictos);
            }
        }

        productos.forEach(p -> p.setOferta(oferta));
        productoRepository.saveAll(productos);
    }

    /**
     * Detecta productos que ya están asignados a OTRA oferta cuyo estado calculado
     * es ACTIVO (RF: no más de una oferta activa por producto en el mismo período).
     * Agrupa por la oferta anterior para que el frontend pueda listar el conflicto.
     */
    private List<ConflictoOfertaDto> detectarConflictos(Oferta ofertaNueva, List<Producto> productos) {
        Map<Integer, List<Producto>> porOfertaAnterior = productos.stream()
                .filter(p -> p.getOferta() != null && !p.getOferta().getIdOferta().equals(ofertaNueva.getIdOferta()))
                .filter(p -> "ACTIVO".equals(calcularEstado(p.getOferta())))
                .collect(Collectors.groupingBy(p -> p.getOferta().getIdOferta()));

        return porOfertaAnterior.values().stream()
                .map(prods -> {
                    Oferta anterior = prods.get(0).getOferta();
                    List<ProductoConflictoDto> productosEnConflicto = prods.stream()
                            .map(p -> new ProductoConflictoDto(p.getIdProducto(), p.getNombre()))
                            .collect(Collectors.toList());
                    return new ConflictoOfertaDto(anterior.getIdOferta(), anterior.getTitulo(), productosEnConflicto);
                })
                .collect(Collectors.toList());
    }

    private void validarDescuentoFijo(OfertaRequestDto req, List<Producto> productos) {
        if (req.getTipoDescuento() != TipoDescuento.MONTO_FIJO) return;
        for (Producto p : productos) {
            if (p.getPrecioBase() != null && req.getValorDescuento() >= p.getPrecioBase()) {
                throw new DatosInvalidosException(String.format(
                        "El descuento fijo de S/ %.2f no puede ser mayor o igual al precio del producto '%s'.",
                        req.getValorDescuento(), p.getNombre()));
            }
        }
    }

    private void desasignarProductosActuales(Integer idOferta) {
        List<Producto> actuales = productoRepository.findByOferta_IdOferta(idOferta);
        actuales.forEach(p -> p.setOferta(null));
        productoRepository.saveAll(actuales);
    }

    private String calcularEstado(Oferta o) {
        if (!Boolean.TRUE.equals(o.getActiva())) return "PAUSADO";
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(o.getFechaInicio())) return "PROGRAMADO";
        if (!now.isAfter(o.getFechaFin()))    return "ACTIVO";
        return "FINALIZADO";
    }

    private OfertaResponseDto toResponseDto(Oferta o) {
        OfertaResponseDto dto = new OfertaResponseDto();
        dto.setIdOferta(o.getIdOferta());
        dto.setIdTienda(o.getIdTienda());
        dto.setTitulo(o.getTitulo());
        dto.setDescripcion(o.getDescripcion());
        dto.setTipoDescuento(o.getTipoDescuento());
        dto.setValorDescuento(o.getValorDescuento());
        dto.setCantidadMinima(o.getCantidadMinima());
        dto.setFechaInicio(o.getFechaInicio());
        dto.setFechaFin(o.getFechaFin());
        dto.setActiva(o.getActiva());
        dto.setEstado(calcularEstado(o));
        dto.setIdsProductos(
                productoRepository.findByOferta_IdOferta(o.getIdOferta()).stream()
                        .map(Producto::getIdProducto)
                        .collect(Collectors.toList())
        );
        return dto;
    }
}
