package com.gamarra360.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `especificaciones`. Pares nombre/descripción técnica de un producto.
 * Ej: nombre="MATERIAL", descripcion="Cuero Top Grain".
 *
 * Renderizada en la sección "Especificaciones Técnicas" del detalle de
 * producto (CU-08).
 */
@Entity
@Table(name = "especificaciones")
@Getter
@Setter
@NoArgsConstructor
public class EspecificacionProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especificacion")
    private Integer idEspecificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;
}
