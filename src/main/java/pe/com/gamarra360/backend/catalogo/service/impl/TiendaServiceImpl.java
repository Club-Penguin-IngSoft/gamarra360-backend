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
        
        // Filtrar productos activos
        List<Producto> productosActivos = tienda.getProductos().stream()
            .filter(p -> p.getActivo() != null && p.getActivo())
            .collect(Collectors.toList());

        // Extraer categorías únicas
        java.util.Set<String> categorias = new java.util.HashSet<>();
        for (Producto p : productosActivos) {
            if (p.getCategoria() != null) {
                categorias.add(p.getCategoria().getNombreCategoria());
            }
        }

        // Extraer tipos de servicio únicos.
        // Orden de prioridad exclusivo (igual que derivarTipoServicio en catalogoService.ts):
        //   1. esPersonalizable=true  → PERSONALIZABLE
        //   2. precioBase null/0      → COTIZACION
        //   3. resto                  → COMPRA_DIRECTA
        java.util.Set<String> tiposServicio = new java.util.HashSet<>();
        for (Producto p : productosActivos) {
            if (Boolean.TRUE.equals(p.getEsPersonalizable())) {
                tiposServicio.add("PERSONALIZABLE");
            } else if (p.getPrecioBase() == null || p.getPrecioBase() == 0) {
                tiposServicio.add("COTIZACION");
            } else {
                tiposServicio.add("COMPRA_DIRECTA");
            }
        }

        // Extraer tipos de producto únicos (Polos, Blusas, etc.)
        java.util.Set<String> tiposProducto = new java.util.HashSet<>();
        for (Producto p : productosActivos) {
            if (p.getTipoProducto() != null && p.getTipoProducto().getNombre() != null) {
                tiposProducto.add(p.getTipoProducto().getNombre());
            }
        }

        // Crear el DTO con los productos activos
        PerfilTiendaPublicaDto dto = new PerfilTiendaPublicaDto();
        dto.setIdTienda(tienda.getIdTienda());
        dto.setNombreComercial(tienda.getNombreComercial());
        dto.setInformacion(tienda.getInformacion());
        dto.setFoto(tienda.getFoto());
        dto.setVerificada(tienda.getVerificada());
        dto.setCategorias(new java.util.ArrayList<>(categorias));
        dto.setTiposServicio(new java.util.ArrayList<>(tiposServicio));
        dto.setTiposProducto(new java.util.ArrayList<>(tiposProducto));

        List<PerfilTiendaPublicaDto.ProductoResumenDto> productosResumen = productosActivos.stream()
            .map(p -> new PerfilTiendaPublicaDto.ProductoResumenDto(
                p.getIdProducto(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecioBase(),
                p.getImagenes() != null && !p.getImagenes().isEmpty()
                    ? p.getImagenes().get(0).getUrl()
                    : null
            ))
            .collect(Collectors.toList());

        dto.setProductos(productosResumen);

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

            // Extraer tipos de servicio únicos (prioridad exclusiva, igual que obtenerPerfilPublico).
            java.util.Set<String> tiposServicio = new java.util.HashSet<>();
            for (Producto p : productosActivos) {
                if (Boolean.TRUE.equals(p.getEsPersonalizable())) {
                    tiposServicio.add("PERSONALIZABLE");
                } else if (p.getPrecioBase() == null || p.getPrecioBase() == 0) {
                    tiposServicio.add("COTIZACION");
                } else {
                    tiposServicio.add("COMPRA_DIRECTA");
                }
            }

            // Extraer tipos de producto únicos (Polos, Blusas, etc.)
            java.util.Set<String> tiposProducto = new java.util.HashSet<>();
            for (Producto p : productosActivos) {
                if (p.getTipoProducto() != null && p.getTipoProducto().getNombre() != null) {
                    tiposProducto.add(p.getTipoProducto().getNombre());
                }
            }

            return new TiendaResumenDto(
                t.getIdTienda(),
                t.getNombreComercial(),
                t.getInformacion(),
                t.getFoto(),
                t.getVerificada(),
                new java.util.ArrayList<>(categorias),
                new java.util.ArrayList<>(tiposServicio),
                new java.util.ArrayList<>(tiposProducto)
            );
        }).collect(java.util.stream.Collectors.toList());

        log.info("Se encontraron {} tiendas públicas", dtos.size());
        return dtos;
    }
    
}
