package com.gamarra360.usuario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `tiendas`. Pertenece a un Comerciante (FK `id_comerciante`).
 *
 * El catálogo público filtra por `tiendas.verificada = true` además de
 * `comerciantes.verificado = true` (CU-07).
 */
@Entity
@Table(name = "tiendas")
@Getter
@Setter
@NoArgsConstructor
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tienda")
    private Integer idTienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comerciante")
    private Comerciante comerciante;

    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @Column(name = "informacion")
    private String informacion;

    @Column(name = "foto")
    private String foto;

    @Column(name = "verificada")
    private Boolean verificada;
}
