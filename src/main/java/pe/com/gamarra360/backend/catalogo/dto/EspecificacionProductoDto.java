package pe.com.gamarra360.backend.catalogo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que mapea a `IEspecificacionProducto` del frontend.
 * Estructura: { nombre, descripcion } — alineado con tabla `especificaciones`.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EspecificacionProductoDto {
    private Integer idEspecificacion;
    private String nombre;
    private String descripcion;
}
