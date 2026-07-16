package pe.com.gamarra360.backend.pedido.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "detalles_pedido")
@Getter
@Setter
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pedido_id")
    private Long pedidoId;
    @Column(name = "id_variante_producto")
    private Integer idVarianteProducto;
    private Integer cantidad;
    private Double precio;
    @Column(name = "personalizacion_id")
    private Long personalizacionId;
    @Column(name = "cotizacion_id")
    private Long cotizacionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", insertable = false, updatable = false)
    @JsonIgnore
    private Pedido pedido;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_variante_producto", insertable = false, updatable = false)
    @JsonIgnore
    private VarianteProducto varianteProducto;
    public DetallePedido() { }
    public Double calcularSubtotal() { return cantidad == null || precio == null ? 0.0 : cantidad * precio; }
}
