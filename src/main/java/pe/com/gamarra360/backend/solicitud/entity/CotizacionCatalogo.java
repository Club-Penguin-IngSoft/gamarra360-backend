package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import jakarta.persistence.*;

@Entity
@Table(name = "cotizaciones_catalogo")
@PrimaryKeyJoinColumn(name = "detalle_cotizacion_id")
@Getter
@Setter
public class CotizacionCatalogo extends DetalleCotizacion {
    @Column(name = "id_detalle_producto")
    private Integer idDetalleProducto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle_producto", insertable = false, updatable = false)
    @JsonIgnore
    private VarianteProducto varianteProducto;
    public CotizacionCatalogo() { }
}
