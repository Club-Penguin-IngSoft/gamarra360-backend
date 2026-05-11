package pe.com.gamarra360.backend.catalogo.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "descuentos_volumen")
@Getter
@Setter
public class DescuentoVolumen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_descuento")
    private Integer idDescuento;
    @Column(name = "cantidad_minima")
    private Integer cantidadMinima;
    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;
    @Column(name = "porcentaje_descuento")
    private Double porcentajeDescuento;
    private Boolean activo;
    @Column(name = "id_producto")
    private Integer idProducto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    @JsonIgnore
    private Producto producto;
    public DescuentoVolumen() { }
}
