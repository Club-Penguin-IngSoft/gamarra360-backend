package pe.com.gamarra360.backend.catalogo.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.catalogo.dto.PerfilTiendaPublicaDto;
import pe.com.gamarra360.backend.catalogo.dto.TiendaResumenDto;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.catalogo.service.TiendaService;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TiendaServiceImpl extends AbstractCrudService<Tienda, Integer> implements TiendaService {

    private final TiendaRepository tiendaRepository; // Inyección explícita

    public TiendaServiceImpl(TiendaRepository repository) {
        super(repository, "Tienda");
        this.tiendaRepository = repository; // Asignación de la instancia inyectada
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
    public PerfilTiendaPublicaDto obtenerPerfilPublico(Integer idTienda) {
        log.info("Obteniendo perfil público de tienda con ID: {}", idTienda);
        
        Tienda tienda = tiendaRepository.findByIdTiendaAndVerificada(idTienda)
            .orElseThrow(() -> {
                log.warn("Tienda con ID {} no encontrada o no verificada", idTienda);
                return new RecursoNoEncontradoException("La tienda solicitada no existe o no está verificada.");
            });
        
        // Crear el DTO con los productos activos
        PerfilTiendaPublicaDto dto = new PerfilTiendaPublicaDto();
        dto.setIdTienda(tienda.getIdTienda());
        dto.setNombreComercial(tienda.getNombreComercial());
        dto.setInformacion(tienda.getInformacion());
        dto.setFoto(tienda.getFoto());
        
        // Filtrar productos activos
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
        
        log.info("Perfil público de tienda {} obtenido exitosamente con {} productos activos", 
                 idTienda, productosActivos.size());
        return dto;
    }
    
    @Override
    public List<TiendaResumenDto> listarTiendasPublicas() {
        log.info("Listando todas las tiendas públicas");

        List<Tienda> tiendasVerificadas = tiendaRepository.findAllByVerificadaTrue();

        // Mapear a DTO de resumen
        List<TiendaResumenDto> dtos = tiendasVerificadas.stream().map(t -> {
            // Filtrar productos activos
            List<Producto> productosActivos = t.getProductos().stream()
                .filter(p -> p.getActivo() != null && p.getActivo())
                .toList();

            // Extraer categorías únicas
            java.util.Set<String> categorias = new java.util.HashSet<>();
            for (Producto p : productosActivos) {
                if (p.getCategoria() != null) {
                    categorias.add(p.getCategoria().getNombreCategoria());
                }
            }

            // Extraer tipos de servicio únicos
            java.util.Set<String> tiposServicio = new java.util.HashSet<>();
            for (Producto p : productosActivos) {
                if (Boolean.TRUE.equals(p.getEsPersonalizable())) {
                    tiposServicio.add("PERSONALIZABLE");
                }
                if (p.getPrecioBase() == null || p.getPrecioBase() == 0) {
                    tiposServicio.add("COTIZACION");
                }
                if (p.getPrecioBase() != null && p.getPrecioBase() > 0 && !Boolean.TRUE.equals(p.getEsPersonalizable())) {
                    tiposServicio.add("COMPRA_DIRECTA");
                }
            }

            return new TiendaResumenDto(
                t.getIdTienda(),
                t.getNombreComercial(),
                t.getInformacion(),
                t.getFoto(),
                t.getVerificada(),
                new java.util.ArrayList<>(categorias),
                new java.util.ArrayList<>(tiposServicio)
            );
        }).collect(java.util.stream.Collectors.toList());

        log.info("Se encontraron {} tiendas públicas", dtos.size());
        return dtos;
    }
    
}
