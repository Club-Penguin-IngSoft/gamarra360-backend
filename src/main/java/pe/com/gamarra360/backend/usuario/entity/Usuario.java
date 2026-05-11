package pe.com.gamarra360.backend.usuario.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.enums.ProveedorAuth;
import pe.com.gamarra360.backend.enums.RolEnum;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    private String nombres;
    @Column(name = "primer_apellido")
    private String primerApellido;
    @Column(name = "segundo_apellido")
    private String segundoApellido;
    @Column(unique = true, nullable = false)
    private String email;
    private String contrasenha;
    private String dni;
    private String telefono;
    private Boolean activo;
    @Enumerated(EnumType.STRING)
    private RolEnum rol;
    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor_auth")
    private ProveedorAuth proveedorAuth;

    public Usuario() {
    }
}
