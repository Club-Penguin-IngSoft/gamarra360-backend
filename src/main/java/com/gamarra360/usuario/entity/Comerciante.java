package com.gamarra360.usuario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `comerciantes`. Especialización de Usuario (relación 1:1 por usuario_id).
 *
 * El catálogo público debe excluir productos cuyo comerciante tenga
 * `verificado = false` (CU-07, RF-20).
 */
@Entity
@Table(name = "comerciantes")
@Getter
@Setter
@NoArgsConstructor
public class Comerciante {

    /**
     * Llave primaria que ES el usuario_id (1:1 con `usuarios`).
     * Mapeo @MapsId comparte la PK con la entidad Usuario.
     */
    @Id
    @Column(name = "usuario_id")
    private Integer usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "ruc")
    private String ruc;

    /**
     * Solo los comerciantes con `verificado = true` pueden mostrar productos
     * en el catálogo público (CU-07).
     */
    @Column(name = "verificado")
    private Boolean verificado;
}
