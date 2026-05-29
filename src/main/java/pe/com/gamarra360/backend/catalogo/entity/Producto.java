package pe.com.gamarra360.backend.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla `productos`. Núcleo del módulo catalogo.
 *
 * CU-08 (RF-22, RF-23): este entity expone todos los campos que la ficha
 * de producto del frontend necesita — descripción, variantes (con stock),
 * imágenes, especificaciones y reglas de descuento por volumen.
 *
 * Para listado en catálogo público, el ProductoService filtra por:
 *  - producto.activo = TRUE
 *  - tienda.verificada = TRUE
 *  - tienda.comerciante.verificado = TRUE
 *  - tienda.comerciante.usuario.activo = TRUE
 */
@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    /** FK de la tienda (columna real en BD — usada por el service para comparaciones rápidas). */
    @Column(name = "id_tienda", insertable = false, updatable = false)
    private Integer idTienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda")
    private Tienda tienda;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio_base")
    private Double precioBase;

    /** Si false, el producto NO debe aparecer en el catálogo público (CU-07) */
    @Column(name = "activo")
    private Boolean activo;

    /** Si true, el producto activa el flujo de personalización en el frontend */
    @Column(name = "es_personalizable")
    private Boolean esPersonalizable;

    /* ----------------------------- Relaciones --------------------------- */

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<VarianteProducto> variantes = new ArrayList<>();

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ImagenProducto> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EspecificacionProducto> especificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DescuentoVolumen> descuentosVolumen = new ArrayList<>();

    /**
     * Relación directa con Categoria por FK id_categoria.
     * El schema define una sola categoria por producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    /**
     * Relación directa con TipoProducto por FK id_tipo_producto.
     * Ejemplo: "Polos", "Blusas", etc. Opcional.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_producto")
    private TipoProducto tipoProducto;
}
