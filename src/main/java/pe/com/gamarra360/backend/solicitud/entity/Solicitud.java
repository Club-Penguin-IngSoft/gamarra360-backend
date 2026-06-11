package pe.com.gamarra360.backend.solicitud.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.enums.EstadoSolicitud;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "solicitudes")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id")
    private Integer clienteId;
    @Column(name = "vendedor_id")
    private Integer vendedorId;
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    @JsonIgnore
    private Cliente cliente;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", insertable = false, updatable = false)
    @JsonIgnore
    private Comerciante vendedor;
    public Solicitud() { }
    @PrePersist void prePersist() { if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); if (estado == null) estado = EstadoSolicitud.PENDIENTE; }
    public void cancelar() { estado = EstadoSolicitud.RECHAZADA; }
    public void marcarComoRespondida() { estado = EstadoSolicitud.RESPONDIDA; }
    public void aceptar() { estado = EstadoSolicitud.ACEPTADA; }
}
