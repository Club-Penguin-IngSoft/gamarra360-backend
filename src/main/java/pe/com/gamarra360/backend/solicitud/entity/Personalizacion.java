package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.enums.TipoTrabajo;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personalizaciones")
@PrimaryKeyJoinColumn(name = "id_personalizacion")
@Getter
@Setter
public class Personalizacion extends Solicitud {
    @Column(name = "detalle_producto_id")
    private Integer detalleProductoId;
    @Column(name = "url_logo")
    private String urlLogo;
    @Column(length = 1000)
    private String descripcion;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_personalizacion")
    private TipoTrabajo tipoPersonalizacion;
    private Integer cantidad;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_producto_id", insertable = false, updatable = false)
    @JsonIgnore
    private VarianteProducto varianteProducto;
    @OneToMany(mappedBy = "personalizacion")
    @JsonIgnore
    private List<DetallePersonalizacion> detallesPersonalizacion = new ArrayList<>();
    public Personalizacion() { }
    public void subirLogo(String url) { this.urlLogo = url; }
    public void actualizarDescripcion(String desc) { this.descripcion = desc; }
    public void seleccionarTipo(TipoTrabajo tipo) { this.tipoPersonalizacion = tipo; }
}
