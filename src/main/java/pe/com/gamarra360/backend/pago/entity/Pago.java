package pe.com.gamarra360.backend.pago.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.enums.EstadoPago;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "pagos")
@Getter
@Setter
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "orden_pago_id")
    private Long ordenPagoId;
    private Double monto;
    @Enumerated(EnumType.STRING)
    private EstadoPago estado;
    private String metodo;
    private LocalDateTime fecha;
    @Column(name= "stripe_payment_intent_id", unique=true)
    private String stripePaymentIntentId;
    @Column(name="stripe_client_secret")
    private String stripeClientSecret;//Solo devuelve al front//
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_pago_id", insertable = false, updatable = false)
    @JsonIgnore
    private OrdenPago ordenPago;
    public Pago() { }
    @PrePersist void prePersist() { if (fecha == null) fecha = LocalDateTime.now(); if (estado == null) estado = EstadoPago.PENDIENTE; }
    public Boolean procesarPago() { estado = EstadoPago.PAGADO; return true; }
    public void confirmarPago() { estado = EstadoPago.PAGADO; }
}
