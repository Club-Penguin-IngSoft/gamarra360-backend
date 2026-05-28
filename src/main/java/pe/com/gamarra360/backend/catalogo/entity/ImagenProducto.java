package pe.com.gamarra360.backend.catalogo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `imagenes_producto`. Galería de imágenes de un producto, con una
 * marcada como `es_principal` (la que se muestra en cards y como thumbnail
 * destacado del detalle).
 */
@Entity
@Table(name = "imagenes_producto")
@Getter
@Setter
@NoArgsConstructor
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    /** URL pública de la imagen (típicamente AWS S3) */
    @Column(name = "url")
    private String url;

    @Column(name = "es_principal")
    private Boolean esPrincipal;
}
