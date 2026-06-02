package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.dto.PerfilTiendaPublicaDto;
import pe.com.gamarra360.backend.catalogo.dto.TiendaInfoResponse;
import pe.com.gamarra360.backend.catalogo.dto.TiendaResumenDto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.catalogo.service.TiendaService;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TiendaServiceImpl extends AbstractCrudService<Tienda, Integer> implements TiendaService {

    private final TiendaRepository tiendaRepository;
    private final ComercianteRepository comercianteRepository;

    public TiendaServiceImpl(TiendaRepository tiendaRepository, ComercianteRepository comercianteRepository) {
        super(tiendaRepository, "Tienda");
        this.tiendaRepository = tiendaRepository;
        this.comercianteRepository = comercianteRepository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Tienda entidad, Integer id) {
        entidad.setIdTienda(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiendaResumenDto> listarPublico() {
        log.info("Listando tiendas verificadas para directorio público");
        return tiendaRepository.findByVerificadaTrue().stream()
                .map(t -> {
                    List<String> categorias = t.getProductos().stream()
                            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                            .map(p -> p.getCategoria() != null ? p.getCategoria().getNombreCategoria() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());

                    List<String> tiposProducto = t.getProductos().stream()
                            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                            .map(p -> p.getTipoProducto() != null ? p.getTipoProducto().getNombre() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());

                    return new TiendaResumenDto(
                            t.getIdTienda(),
                            t.getNombreComercial(),
                            t.getInformacion(),
                            t.getFoto(),
                            t.getVerificada(),
                            categorias,
                            null,           // tiposServicio: sin dato en BD; frontend usa ['COMPRA_DIRECTA']
                            tiposProducto
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TiendaInfoResponse obtenerInfoComerciante(Integer comercianteId) {
        log.info("Obteniendo info de tienda para comerciante {}", comercianteId);
        Tienda tienda = tiendaRepository.findByIdComerciante(comercianteId)
                .orElseThrow(() -> new DatosInvalidosException("No tienes una tienda asignada."));
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado"));
        return new TiendaInfoResponse(tienda.getIdTienda(), tienda.getNombreComercial(), comerciante.getRuc());
    }

    @Override
    public PerfilTiendaPublicaDto obtenerPerfilPublico(Integer idTienda) {
        log.info("Obteniendo perfil público de tienda con ID: {}", idTienda);

        Tienda tienda = tiendaRepository.findByIdTiendaAndVerificada(idTienda)
            .orElseThrow(() -> {
                log.warn("Tienda con ID {} no encontrada o no verificada", idTienda);
                return new RecursoNoEncontradoException("La tienda solicitada no existe o no está verificada.");
            });

        PerfilTiendaPublicaDto dto = new PerfilTiendaPublicaDto();
        dto.setIdTienda(tienda.getIdTienda());
        dto.setNombreComercial(tienda.getNombreComercial());
        dto.setInformacion(tienda.getInformacion());
        dto.setFoto(tienda.getFoto());

        java.util.List<PerfilTiendaPublicaDto.ProductoResumenDto> productosActivos = tienda.getProductos()
            .stream()
            .filter(p -> p.getActivo() != null && p.getActivo())
            .map(p -> new PerfilTiendaPublicaDto.ProductoResumenDto(
                p.getIdProducto(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecioBase(),
                p.getImagenes() != null && !p.getImagenes().isEmpty()
                    ? p.getImagenes().get(0).getUrl()
                    : null
            ))
            .collect(java.util.stream.Collectors.toList());

        dto.setProductos(productosActivos);

        log.info("Perfil público de tienda {} obtenido con {} productos activos",
                 idTienda, productosActivos.size());
        return dto;
    }
}