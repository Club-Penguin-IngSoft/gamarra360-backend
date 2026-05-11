package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "detalles_cotizacion")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class DetalleCotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_cotizacion_id")
    private Integer detalleCotizacionId;
    private Integer cantidad;
    @Column(name = "precio_base")
    private Double precioBase;
    @Column(name = "id_cotizacion")
    private Long idCotizacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cotizacion", insertable = false, updatable = false)
    @JsonIgnore
    private Cotizacion cotizacion;
    public DetalleCotizacion() { }
}
