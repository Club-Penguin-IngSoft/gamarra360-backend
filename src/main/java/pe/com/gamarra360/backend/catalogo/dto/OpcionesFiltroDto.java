package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respuesta del endpoint GET /api/v1/productos/opciones-filtro.
 *
 * Contiene los valores disponibles para cada filtro del catálogo,
 * derivados de los datos reales de la BD (no hardcodeados en el frontend).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpcionesFiltroDto {
    /** Nombres de colores activos (ej. "Azul marino", "Blanco", "Negro") */
    private List<String> colores;
    /** Materiales distintos presentes en especificaciones (ej. "Algodón", "Denim") */
    private List<String> materiales;
    /** Tallas activas (ej. "S", "M", "L", "XL") */
    private List<String> tallas;
    /** Nombres únicos de tipos de producto (ej. "Blusas", "Pantalones", "Polos") */
    private List<String> tiposProducto;
    /** Nombres de categorías (ej. "Niños", "Adultos", "Damas") */
    private List<String> categorias;
}
