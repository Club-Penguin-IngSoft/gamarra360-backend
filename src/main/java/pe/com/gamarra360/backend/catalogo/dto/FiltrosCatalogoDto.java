package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de filtros que recibe el ProductoController vía @RequestParam.
 * Alineado con `IFiltrosCatalogo` del frontend.
 *
 * Spring MVC mapea automáticamente cada query param al campo del mismo nombre:
 *   GET /productos?q=polo&categorias=HOMBRE&categorias=MUJER&precioMin=20
 * →
 *   FiltrosCatalogoDto { q="polo", categorias=["HOMBRE","MUJER"], precioMin=20 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltrosCatalogoDto {

    /** Búsqueda por palabras clave — RF-22, RF-23 (CU-08) */
    private String q;

    /** "DOMICILIO" | "TIENDA" */
    private String entrega;

    /** "COMPRA_DIRECTA" | "PERSONALIZABLE" | "COTIZACION" */
    private String tipoServicio;

    /** Categorías a filtrar (multi-select) */
    private List<String> categorias;

    /** Tipos de producto (Polos, Blusas, etc.) — string libre */
    private List<String> tiposProducto;

    private String color;
    private String material;
    private List<String> tallas;

    private Double precioMin;
    private Double precioMax;

    /** Ordenamiento: RECENT | PRICE_ASC | PRICE_DESC | RELEVANCIA */
    private String sort;

    /** Página (1-based) */
    private Integer page;

    /** Tamaño de página */
    private Integer size;

    /** Randomiza resultados (para home) */
    private Boolean random;

    /** Semilla opcional para random determinístico */
    private Long seed;
}
