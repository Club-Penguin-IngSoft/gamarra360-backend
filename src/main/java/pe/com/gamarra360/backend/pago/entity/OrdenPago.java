package pe.com.gamarra360.backend.pago.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.enums.EstadoPago;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "ordenes_pago")
@Getter
@Setter
public class OrdenPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id")
    private Integer clienteId;
    private Double total;
    @Enumerated(EnumType.STRING)
    private EstadoPago estado;
    private LocalDateTime fecha;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    @JsonIgnore
    private Cliente cliente;
    @OneToMany(mappedBy = "ordenPago")
    @JsonIgnore
    private List<Pedido> pedidos = new ArrayList<>();
    @OneToOne(mappedBy = "ordenPago")
    @JsonIgnore
    private Pago pago;
    public OrdenPago() { }
    @PrePersist void prePersist() { if (fecha == null) fecha = LocalDateTime.now(); if (estado == null) estado = EstadoPago.PENDIENTE; }
    public void confirmarPago() { estado = EstadoPago.PAGADO; }
    public void cancelar() { estado = EstadoPago.FALLIDO; }
}
