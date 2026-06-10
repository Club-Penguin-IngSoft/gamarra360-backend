package pe.com.gamarra360.backend.usuario.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import pe.com.gamarra360.backend.pedido.entity.Carrito;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
public class Cliente extends Usuario {
    private String nombre;
    private String apellido;

    @OneToOne(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonIgnore
    private Carrito carrito;

    public Cliente() {
    }
    @Column(name = "direccion_entrega", length = 500)
    private String direccionEntrega;

    @Column(name = "alertas_correo")
    private Boolean alertasCorreo = true;

    @Column(name = "notificaciones_push")
    private Boolean notificacionesPush = false;
}
