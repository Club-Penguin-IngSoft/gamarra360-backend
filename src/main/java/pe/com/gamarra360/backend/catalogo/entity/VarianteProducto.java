package pe.com.gamarra360.backend.catalogo.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "variantes_producto")
@Getter
@Setter
public class VarianteProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variante")
    private Integer idVariante;
    private String sku;
    private Integer stock;
    @Column(name = "minimo_stock")
    private Integer minimoStock;
    @Column(name = "precio_ajustado")
    private Double precioAjustado;
    private Boolean disponible;
    @Column(name = "id_producto")
    private Integer idProducto;
    @Column(name = "id_talla")
    private Integer idTalla;
    @Column(name = "id_color")
    private Integer idColor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    @JsonIgnore
    private Producto producto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_talla", insertable = false, updatable = false)
    @JsonIgnore
    private Talla tallaRef;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_color", insertable = false, updatable = false)
    @JsonIgnore
    private Color colorRef;
    public VarianteProducto() { }
}
