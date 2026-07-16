package pe.com.gamarra360.backend.logistica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distritos_envio")
@Getter
@Setter
@NoArgsConstructor
public class DistritoEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_distrito")
    private Integer idDistrito;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "costo_envio")
    private Double costoEnvio;

    @Column(name = "activo")
    private Boolean activo;
}
