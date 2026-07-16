package pe.com.gamarra360.backend.usuario.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "notificaciones")
@Getter
@Setter
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;
    @Column(name = "usuario_id")
    private Integer usuarioId;

    private String mensaje;
    private Boolean fueleida;
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    private String tipo;
    @Column(name = "actor_id")
    private Integer actorId; // quien lo hizo

    @Column(name = "referencia_id")
    private Long referenciaId; // cotización/pedido/etc

    @Column(name = "referencia_tipo")
    private String referenciaTipo; // COTIZACION / PEDIDO

    @Column(name = "estado_referencia")
    private String estadoReferencia; // estado real

    @Column(name = "ruta")
    private String ruta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    @JsonIgnore
    private Usuario usuario;

    public Notificacion() {
    }

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (fueleida == null) fueleida = false;
    }
}
