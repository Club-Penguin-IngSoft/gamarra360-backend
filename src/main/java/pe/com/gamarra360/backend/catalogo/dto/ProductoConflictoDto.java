package pe.com.gamarra360.backend.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Producto puntual afectado por un conflicto de oferta activa vigente. */
@Data
@AllArgsConstructor
public class ProductoConflictoDto {
    private Integer idProducto;
    private String nombre;
}
