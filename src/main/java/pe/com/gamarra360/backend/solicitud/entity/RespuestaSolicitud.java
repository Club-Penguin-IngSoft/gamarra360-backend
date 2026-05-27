package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "respuestas_solicitud")
@Getter
@Setter
public class RespuestaSolicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Long idRespuesta;
    @Column(name = "id_solicitud")
    private Long idSolicitud;
    @Column(name = "precio_propuesto")
    private Double precioPropuesto;
    private LocalDateTime fecha;
    private String anotaciones;
    private String comentario;
    private String condiciones;
    private String imagen;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", insertable = false, updatable = false)
    @JsonIgnore
    private Solicitud solicitud;
    @OneToOne(mappedBy = "respuestaSolicitud")
    @JsonIgnore
    private ItemPersonalizado itemPersonalizado;
    public RespuestaSolicitud() { }
    @PrePersist void prePersist() { if (fecha == null) fecha = LocalDateTime.now(); }
    public Boolean vencida(LocalDateTime fechaLimite) { return fechaLimite != null && LocalDateTime.now().isAfter(fechaLimite); }
}
