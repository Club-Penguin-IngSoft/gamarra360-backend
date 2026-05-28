package pe.com.gamarra360.backend.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `colores`. Catálogo de colores disponibles para variantes
 * (Negro #1A1A1A, Blanco #F5F5F5, Azul Marino #1B2A4E, etc.).
 */
@Entity
@Table(name = "colores")
@Getter
@Setter
@NoArgsConstructor
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_color")
    private Integer idColor;

    @Column(name = "nombre")
    private String nombre;

    /** Código hexadecimal para mostrar el swatch en la UI (ej. "#1A1A1A") */
    @Column(name = "cod_hex")
    private String codHex;

    @Column(name = "activo")
    private Boolean activo;
}
