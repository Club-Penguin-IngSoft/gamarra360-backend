package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "items_personalizados")
@Getter
@Setter
public class ItemPersonalizado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id")
    private Integer clienteId;
    @Column(name = "vendedor_id")
    private Integer vendedorId;
    @Column(name = "detalle_producto_id")
    private Integer detalleProductoId;
    private Integer cantidad;
    @Column(name = "precio_acordado")
    private Double precioAcordado;
    @Column(name = "url_logo")
    private String urlLogo;
    @Column(length = 1000)
    private String descripcion;
    @Column(name = "respuesta_id")
    private Long respuestaId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    @JsonIgnore
    private Cliente cliente;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", insertable = false, updatable = false)
    @JsonIgnore
    private Comerciante vendedor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_producto_id", insertable = false, updatable = false)
    @JsonIgnore
    private VarianteProducto varianteProducto;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", insertable = false, updatable = false)
    @JsonIgnore
    private RespuestaSolicitud respuestaSolicitud;
    public ItemPersonalizado() { }
}
