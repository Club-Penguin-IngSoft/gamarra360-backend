package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.enums.TipoTrabajo;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "detalles_personalizacion")
@Getter
@Setter
public class DetallePersonalizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_personalizacion")
    private Integer idDetallePersonalizacion;
    @Column(name = "id_personalizacion")
    private Long idPersonalizacion;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_trabajo")
    private TipoTrabajo tipoTrabajo;
    private String posicion;
    private Double alto;
    private Double ancho;
    @Column(name = "instrucciones_adicionales", length = 1000)
    private String instruccionesAdicionales;
    private String texto;
    private String color;
    @Lob
    private byte[] archivo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personalizacion", insertable = false, updatable = false)
    @JsonIgnore
    private Personalizacion personalizacion;
    public DetallePersonalizacion() { }
    public byte[] getArchivo() { return archivo; }
}
