package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cotizaciones")
@PrimaryKeyJoinColumn(name = "id_cotizacion")
@Getter
@Setter
public class Cotizacion extends Solicitud {
    @Column(name = "id_tienda")
    private Integer idTienda;
    @Column(name = "precio_deseado")
    private Double precioDeseado;
    @Column(name = "pedido_id")
    private Long pedidoId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", insertable = false, updatable = false)
    @JsonIgnore
    private Tienda tienda;
    @OneToMany(mappedBy = "cotizacion")
    @JsonIgnore
    private List<DetalleCotizacion> listaDetallesCotizacion = new ArrayList<>();
    @OneToOne(mappedBy = "solicitud")
    @JsonIgnore
    private RespuestaSolicitud respuestaSolicitud;
    public Cotizacion() { }
}
