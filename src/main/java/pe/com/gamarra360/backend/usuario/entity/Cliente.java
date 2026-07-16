package pe.com.gamarra360.backend.usuario.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import pe.com.gamarra360.backend.logistica.entity.DistritoEnvio;
import pe.com.gamarra360.backend.pedido.entity.Carrito;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
public class Cliente extends Usuario {

    @Transient
    private String nombre;

    @Transient
    private String apellido;

    @Column(name= "direccion_entrega")
    private String direccionEntrega;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "id_distrito")
    private Integer idDistrito;

    @Column(name = "alertas_correo")
    private Boolean alertasCorreo = false;

    @Column(name = "notificaciones_push")
    private Boolean notificacionesPush = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_distrito", insertable = false, updatable = false)
    @JsonIgnore
    private DistritoEnvio distrito;

    @OneToOne(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonIgnore
    private Carrito carrito;

    public Cliente() {
    }
}
