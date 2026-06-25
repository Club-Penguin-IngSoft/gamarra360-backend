package pe.com.gamarra360.backend.pedido.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.enums.TipoEntrega;
import pe.com.gamarra360.backend.logistica.entity.DistritoEnvio;
import pe.com.gamarra360.backend.pago.entity.OrdenPago;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id")
    private Integer clienteId;
    @Column(name = "vendedor_id")
    private Integer vendedorId;
    @Column(name = "orden_pago_id")
    private Long ordenPagoId;
    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;
    private Double total;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega")
    private TipoEntrega tipoEntrega;
    @Column(name = "direccion_entrega")
    private String direccionEntrega;
    private LocalDateTime fecha;
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    @Column(name = "costo_envio")
    private Double costoEnvio;
    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;
    @Column(name = "id_distrito")
    private Integer idDistrito;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_distrito", insertable = false, updatable = false)
    @JsonIgnore
    private DistritoEnvio distrito;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    @JsonIgnore
    private Cliente cliente;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", insertable = false, updatable = false)
    @JsonIgnore
    private Comerciante vendedor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_pago_id", insertable = false, updatable = false)
    @JsonIgnore
    private OrdenPago ordenPago;
    @OneToMany(mappedBy = "pedido")
    @JsonIgnore
    private List<DetallePedido> listaDetalles = new ArrayList<>();
    public Pedido() { }
    @PrePersist void prePersist() { if (fecha == null) fecha = LocalDateTime.now(); if (fechaActualizacion == null) fechaActualizacion = fecha; if (estado == null) estado = EstadoPedido.RECIBIDO; }
    public Double calcularTotal() { return listaDetalles == null ? 0.0 : listaDetalles.stream().mapToDouble(DetallePedido::calcularSubtotal).sum(); }
    public void cambiarEstado(EstadoPedido estado) { this.estado = estado; this.fechaActualizacion = LocalDateTime.now(); }
    public Boolean validarEntrega() { return tipoEntrega != null && (tipoEntrega == TipoEntrega.RECOJO_TIENDA || direccionEntrega != null); }
}
