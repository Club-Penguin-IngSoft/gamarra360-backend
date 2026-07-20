package pe.com.gamarra360.backend.logistica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tarifas_envio_tienda", uniqueConstraints = @UniqueConstraint(columnNames = {"id_tienda", "id_distrito"}))
@Getter
@Setter
public class TarifaEnvioTienda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa") private Long id;
    @Column(name = "id_tienda", nullable = false) private Integer tiendaId;
    @Column(name = "id_distrito", nullable = false) private Integer distritoId;
    @Column(name = "costo_envio", nullable = false) private Double costoEnvio;
    @Column(nullable = false) private Boolean activo = true;
}
