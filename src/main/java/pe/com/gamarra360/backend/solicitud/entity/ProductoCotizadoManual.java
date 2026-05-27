package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "productos_cotizados_manual")
@Getter
@Setter
public class ProductoCotizadoManual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "detalle_cotizacion_id")
    private Integer detalleCotizacionId;
    private String nombre;
    private String especificacion;
    private String imagen;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_cotizacion_id", insertable = false, updatable = false)
    @JsonIgnore
    private DetalleCotizacion detalleCotizacion;
    public ProductoCotizadoManual() { }
}
