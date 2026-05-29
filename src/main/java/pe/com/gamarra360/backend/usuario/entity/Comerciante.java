package pe.com.gamarra360.backend.usuario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;

/**
 * Tabla `comerciantes`. Especialización de Usuario (herencia JOINED por usuario_id).
 * Solo los comerciantes con `verificado = true` pueden mostrar productos
 * en el catálogo público (CU-07, RF-20).
 */
@Entity
@Table(name = "comerciantes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
public class Comerciante extends Usuario {

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "ruc")
    private String ruc;

    @Column(name = "verificado")
    private Boolean verificado;

    //private String estado;//no se usa
    //@Column(name = "motivo_rechazo")
    //private String motivoRechazo;//no se usa
    @Column(name = "nombre_tienda")
    private String nombreTienda;
    @Column(name = "primer_nombre")
    private String nombreComerciante;
    @Column(name = "primer_apellido")
    private String apellidoComerciante;
    @Column(name = "aprobado")
    private Boolean aprobado;
    @OneToOne(mappedBy = "comerciante", fetch = FetchType.LAZY)
    @JsonIgnore
    private Tienda tienda;
}
