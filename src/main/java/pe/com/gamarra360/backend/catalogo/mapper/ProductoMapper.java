package pe.com.gamarra360.backend.catalogo.mapper;

import pe.com.gamarra360.backend.catalogo.dto.*;
import pe.com.gamarra360.backend.catalogo.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Mapper Entity ↔ DTO para Producto y entidades relacionadas.
 *
 * Responsabilidades:
 *  - Convertir Producto y sus relaciones lazy en ProductoDto plano para JSON.
 *  - Derivar `tipoServicio` a partir de `esPersonalizable` (la BD no tiene
 *    el enum, la UI sí).
 *  - Calcular `precioFinal` aplicando la mejor regla de descuento por volumen
 *    activa (para cantidad=1, que es la vista catálogo).
 *  - Ordenar imágenes para que la `esPrincipal` quede primera.
 *
 * Patrón Data Mapper (CLAUDE.md §6). Inyectable como bean Spring.
 */
@Component
public class ProductoMapper {

    /* =========================================================================
       Entity → DTO
       ========================================================================= */

    /** Versión liviana para listados (catálogo) — sin variantes/specs/imágenes detalladas */
    public ProductoDto toResumenDto(Producto p) {
        if (p == null) return null;

        Double precioFinal = calcularPrecioFinal(p.getPrecioBase(), p.getDescuentosVolumen());

        return ProductoDto.builder()
                .id(String.valueOf(p.getIdProducto()))
                .titulo(p.getNombre())
                .descripcion(p.getDescripcion())
                .activo(p.getActivo())
                .esPersonalizable(Boolean.TRUE.equals(p.getEsPersonalizable()))
                .idTienda(tiendaIdString(p))
                .idComerciante(comercianteIdString(p))
                .nombreTienda(nombreTienda(p))
                .imagenes(imagenesOrdenadas(p.getImagenes()))
                .categoria(categoriaPrincipal(p.getCategorias()))
                .tipoServicio(derivarTipoServicio(p.getEsPersonalizable()))
                .precioBase(p.getPrecioBase())
                .precioFinal(precioFinal)
                .build();
    }

    /** Versión completa para detalle (CU-08) — incluye todo */
    public ProductoDto toDetalleDto(Producto p) {
        if (p == null) return null;

        ProductoDto base = toResumenDto(p);
        base.setVariantes(toVarianteDtoList(p.getVariantes()));
        base.setEspecificaciones(toEspecificacionDtoList(p.getEspecificaciones()));
        base.setDescuentosVolumen(toDescuentoDtoList(p.getDescuentosVolumen()));
        return base;
    }

    /* =========================================================================
       Helpers privados
       ========================================================================= */

    /**
     * Deriva el tipo de servicio visible a partir del bit `es_personalizable`.
     * NOTA: la BD no tiene un enum "tipo_servicio"; COTIZACION es un flujo
     * aparte (tabla `solicitudes`), no un atributo del producto.
     */
    private String derivarTipoServicio(Boolean esPersonalizable) {
        return Boolean.TRUE.equals(esPersonalizable) ? "PERSONALIZABLE" : "COMPRA_DIRECTA";
    }

    /**
     * Aplica la mejor regla de descuento por volumen activa para cantidad=1.
     * En el catálogo, mostramos el precio "desde" — el mejor descuento disponible.
     *
     * Estrategia: tomar el descuento con menor cantidad_minima entre los activos
     * (el que aplica al cliente que compra menos).
     */
    private Double calcularPrecioFinal(Double precioBase, List<DescuentoVolumen> descuentos) {
        if (precioBase == null) return null;
        if (descuentos == null || descuentos.isEmpty()) return precioBase;

        Optional<DescuentoVolumen> mejor = descuentos.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .min(Comparator.comparing(DescuentoVolumen::getCantidadMinima));

        return mejor
                .map(d -> precioBase * (1.0 - d.getPorcentajeDescuento() / 100.0))
                .orElse(precioBase);
    }

    /**
     * Devuelve la categoría "principal" del producto (la primera asociada).
     * Si la N:M tiene múltiples, simplificamos a una sola para la UI.
     */
    private String categoriaPrincipal(List<Categoria> categorias) {
        if (categorias == null || categorias.isEmpty()) return "UNISEX_ADULTOS";
        return categorias.get(0).getNombreCategoria();
    }

    /**
     * Devuelve URLs de imágenes ordenadas: la principal primero, luego el resto.
     */
    private List<String> imagenesOrdenadas(List<ImagenProducto> imagenes) {
        if (imagenes == null || imagenes.isEmpty()) return Collections.emptyList();
        return imagenes.stream()
                .sorted(Comparator.comparing(
                        (ImagenProducto i) -> Boolean.TRUE.equals(i.getEsPrincipal()) ? 0 : 1))
                .map(ImagenProducto::getUrl)
                .toList();
    }

    private String tiendaIdString(Producto p) {
        if (p.getTienda() == null) return null;
        return String.valueOf(p.getTienda().getIdTienda());
    }

    private String comercianteIdString(Producto p) {
        if (p.getTienda() == null || p.getTienda().getComerciante() == null) return null;
        return String.valueOf(p.getTienda().getComerciante().getUsuarioId());
    }

    private String nombreTienda(Producto p) {
        if (p.getTienda() == null) return null;
        return p.getTienda().getNombreComercial();
    }

    /* ---------------- Mappers de listas anidadas ---------------- */

    private List<VarianteProductoDto> toVarianteDtoList(List<VarianteProducto> variantes) {
        if (variantes == null) return Collections.emptyList();
        return variantes.stream().map(this::toVarianteDto).toList();
    }

    private VarianteProductoDto toVarianteDto(VarianteProducto v) {
        return VarianteProductoDto.builder()
                .id(String.valueOf(v.getIdVariante()))
                .sku(v.getSku())
                .talla(v.getTalla() != null ? v.getTalla().getTalla() : null)
                .color(v.getColor() != null ? v.getColor().getNombre() : null)
                .colorHex(v.getColor() != null ? v.getColor().getCodHex() : null)
                .stock(v.getStock())
                .minimoStock(v.getMinimoStock())
                .precioAjustado(v.getPrecioAjustado())
                .disponible(v.getDisponible())
                .build();
    }

    private List<EspecificacionProductoDto> toEspecificacionDtoList(List<EspecificacionProducto> specs) {
        if (specs == null) return Collections.emptyList();
        return specs.stream().map(this::toEspecificacionDto).toList();
    }

    private EspecificacionProductoDto toEspecificacionDto(EspecificacionProducto e) {
        return EspecificacionProductoDto.builder()
                .idEspecificacion(e.getIdEspecificacion())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .build();
    }

    private List<DescuentoVolumenDto> toDescuentoDtoList(List<DescuentoVolumen> descuentos) {
        if (descuentos == null) return Collections.emptyList();
        return descuentos.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .map(this::toDescuentoDto)
                .toList();
    }

    private DescuentoVolumenDto toDescuentoDto(DescuentoVolumen d) {
        return DescuentoVolumenDto.builder()
                .idDescuento(d.getIdDescuento())
                .cantidadMinima(d.getCantidadMinima())
                .cantidadMaxima(d.getCantidadMaxima())
                .porcentajeDescuento(d.getPorcentajeDescuento())
                .activo(d.getActivo())
                .build();
    }
}
