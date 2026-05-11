package pe.com.gamarra360.backend.pedido.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "items_carrito")
@Getter
@Setter
public class ItemCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "carrito_id")
    private Long carritoId;
    @Column(name = "id_variante_producto")
    private Integer idVarianteProducto;
    private Integer cantidad;
    @Column(name = "precio_unitario")
    private Double precioUnitario;
    @Column(name = "vendedor_id")
    private Integer vendedorId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", insertable = false, updatable = false)
    @JsonIgnore
    private Carrito carrito;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_variante_producto", insertable = false, updatable = false)
    @JsonIgnore
    private VarianteProducto varianteProducto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", insertable = false, updatable = false)
    @JsonIgnore
    private Comerciante vendedor;
    public ItemCarrito() { }
    public Double calcularSubtotal() { return cantidad == null || precioUnitario == null ? 0.0 : cantidad * precioUnitario; }
}
