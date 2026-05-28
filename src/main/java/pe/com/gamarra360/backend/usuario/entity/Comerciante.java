package pe.com.gamarra360.backend.usuario.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;

@Entity
@Table(name = "comerciantes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
public class Comerciante extends Usuario {
    private String ruc;
    @Column(name = "razon_social")
    private String razonSocial;
    private Boolean verificado;
    @Column(name = "id_tienda")
    private Long idTienda;
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
    public Comerciante() {
    }
}
