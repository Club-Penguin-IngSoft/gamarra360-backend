package pe.com.gamarra360.backend.catalogo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `variantes_producto`. Cada combinación talla × color de un producto
 * con su stock y precio ajustado opcional.
 *
 * Se muestra en la ficha de producto (CU-08) como swatches de color y
 * pills de talla. El `stock` real se usa para mensajes "¡ÚLTIMAS N UNIDADES!"
 * y para deshabilitar variantes sin stock.
 */
@Entity
@Table(name = "variantes_producto")
@Getter
@Setter
@NoArgsConstructor
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variante")
    private Integer idVariante;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_color")
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_talla")
    private Talla talla;

    /** SKU único de la variante */
    @Column(name = "sku")
    private String sku;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "minimo_stock")
    private Integer minimoStock;

    /**
     * Precio específico de esta variante. Si está definido, sobreescribe
     * `producto.precioBase`. Útil cuando una talla XXL o color especial
     * tiene precio diferente.
     */
    @Column(name = "precio_ajustado")
    private Double precioAjustado;

    @Column(name = "disponible")
    private Boolean disponible;

    @Column(name = "imagen_url")
    private String imagenUrl;
}
