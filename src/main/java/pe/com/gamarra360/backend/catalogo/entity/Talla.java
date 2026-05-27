package com.gamarra360.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `tallas`. Catálogo de tallas (S, M, L, XL, etc.).
 */
@Entity
@Table(name = "tallas")
@Getter
@Setter
@NoArgsConstructor
public class Talla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_talla")
    private Integer idTalla;

    @Column(name = "talla")
    private String talla;

    @Column(name = "activo")
    private Boolean activo;
}
