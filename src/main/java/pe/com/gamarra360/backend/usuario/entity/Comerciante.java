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
    private String estado;
    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @OneToOne(mappedBy = "comerciante", fetch = FetchType.LAZY)
    @JsonIgnore
    private Tienda tienda;

    public Comerciante() {
    }
}
