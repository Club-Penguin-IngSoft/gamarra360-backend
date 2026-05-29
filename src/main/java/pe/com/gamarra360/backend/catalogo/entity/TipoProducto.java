package pe.com.gamarra360.backend.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `tipo_producto`.
 * Cada tipo pertenece a una categoria (e.g. "Polos" -> Hombre).
 * Relation: tipo_producto.id_categoria -> categorias(id_categoria)
 */
@Entity
@Table(name = "tipo_producto")
@Getter
@Setter
@NoArgsConstructor
public class TipoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_producto")
    private Integer idTipoProducto;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
}
