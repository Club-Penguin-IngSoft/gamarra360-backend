package pe.com.gamarra360.backend.catalogo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que mapea a `IDescuentoVolumen` del frontend.
 *
 * Regla de descuento aplicable cuando la cantidad pedida cae en el rango
 * [cantidadMinima, cantidadMaxima].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescuentoVolumenDto {
    private Integer idDescuento;
    private Integer cantidadMinima;
    private Integer cantidadMaxima;
    private Double porcentajeDescuento;
    private Boolean activo;
}
