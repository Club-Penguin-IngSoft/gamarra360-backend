package pe.com.gamarra360.backend.solicitud.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_personalizacion")
@Getter
@Setter
public class MensajePersonalizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long id;

    @Column(name = "id_personalizacion", nullable = false)
    private Long personalizacionId;

    @Column(name = "id_remitente", nullable = false)
    private Integer remitenteId;

    @Column(nullable = false, length = 1500)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
