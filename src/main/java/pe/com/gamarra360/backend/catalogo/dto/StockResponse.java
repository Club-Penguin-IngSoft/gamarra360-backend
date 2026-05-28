package pe.com.gamarra360.backend.catalogo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockResponse {
    private Integer idVariante;
    private String sku;
    private Integer stock;
    private Boolean disponible;
    private Integer idProducto;
}