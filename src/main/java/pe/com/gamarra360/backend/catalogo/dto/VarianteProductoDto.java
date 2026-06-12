package pe.com.gamarra360.backend.catalogo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que mapea a `IVarianteProducto` del frontend (Frontend/src/types/IProducto.ts).
 *
 * Campos `null` se omiten en el JSON para mantenerlo ligero
 * (@JsonInclude(NON_NULL)).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VarianteProductoDto {
    private String id;          // toString del idVariante
    private String sku;
    private String talla;
    private String color;
    private String colorHex;
    private Integer stock;
    private Integer minimoStock;
    private Double precioAjustado;
    private Boolean disponible;
    private String imagenUrl;
}
