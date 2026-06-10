package pe.com.gamarra360.backend.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity(name = "CatalogoProductoCotizadoManual")
@Table(name = "producto_cotizado_manual")
@Getter
@Setter
@NoArgsConstructor

public class ProductoCotizadoManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con el detalle de cotización
    @Column(name = "detalle_cotizacion_id", nullable = false)
    private Integer detalleCotizacionId;

    // Producto relacionado (puede ser nulo si es un producto manual)
    @Column(name = "producto_id")
    private Integer productoId;

    // Cantidad cotizada
    @Column(name = "cantidad")
    private Integer cantidad;

    // Precio unitario
    @Column(name = "precio_unitario")
    private Double precioUnitario;

    // Observaciones opcionales
    @Column(name = "observaciones", length = 500)
    private String observaciones;
}