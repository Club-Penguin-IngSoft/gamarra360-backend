package com.gamarra360.usuario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `usuarios`. Mínimo necesario para que el módulo catalogo pueda
 * navegar la cadena Producto → Tienda → Comerciante → Usuario y validar el
 * filtrado de comerciantes activos (CU-07).
 *
 * El módulo `usuario` (CU-01 a CU-06) extenderá esta entidad con campos
 * adicionales (proveedor_auth, rol, telefono, etc.).
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "nombres")
    private String nombres;

    @Column(name = "primer_apellido")
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Si `activo = false`, el usuario (y por lo tanto sus tiendas) NO deben
     * aparecer en el catálogo público. Se valida en los filtros de catalogo.
     */
    @Column(name = "activo")
    private Boolean activo;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Rol rol;

    public enum Rol {
        ADMIN, CLIENTE, VENDEDOR
    }
}
