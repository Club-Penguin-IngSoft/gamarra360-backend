package pe.com.gamarra360.backend.catalogo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `descuentos_volumen`. Regla de descuento por volumen para un producto.
 *
 * Ej: si compras 10-49 unidades → 5% off. Si compras 50+ → 10% off.
 *
 * Patrón Chain of Responsibility (CLAUDE.md §6): se evalúa la regla con
 * `cantidad_minima ≤ cantidad ≤ cantidad_maxima` que mejor aplique.
 *
 * En la UI del catálogo se aplica la regla mínima (la de menor cantidad)
 * para mostrar el "precio con descuento" visible (CU-08, RF-22).
 */
@Entity
@Table(name = "descuentos_volumen")
@Getter
@Setter
@NoArgsConstructor
public class DescuentoVolumen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_descuento")
    private Integer idDescuento;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "cantidad_minima")
    private Integer cantidadMinima;

    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;

    @Column(name = "porcentaje_descuento")
    private Double porcentajeDescuento;

    @Column(name = "activo")
    private Boolean activo;
}
