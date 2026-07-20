package pe.com.gamarra360.backend.reclamo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamos")
@Getter
@Setter
public class Reclamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reclamo")
    private Long id;
    @Column(name = "id_cliente", nullable = false)
    private Integer clienteId;
    @Column(name = "id_vendedor")
    private Integer vendedorId;
    @Column(name = "id_pedido")
    private Long pedidoId;
    @Column(nullable = false, length = 30)
    private String tipo;
    @Column(nullable = false, length = 200)
    private String asunto;
    @Column(nullable = false, length = 3000)
    private String descripcion;
    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";
    @Column(length = 3000)
    private String respuesta;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
}
