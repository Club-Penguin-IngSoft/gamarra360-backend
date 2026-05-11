package pe.com.gamarra360.backend.catalogo.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;
    private String nombre;
    @Column(length = 1000)
    private String descripcion;
    @Column(name = "precio_base")
    private Double precioBase;
    @Column(name = "es_personalizable")
    private Boolean esPersonalizable;
    private Boolean activo;
    @Column(name = "id_tienda")
    private Integer idTienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", insertable = false, updatable = false)
    @JsonIgnore
    private Tienda tienda;

    @ManyToMany
    @JoinTable(name = "producto_categoria", joinColumns = @JoinColumn(name = "id_producto"), inverseJoinColumns = @JoinColumn(name = "id_categoria"))
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private List<Especificacion> especificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private List<ImagenProducto> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private List<DescuentoVolumen> descuentos = new ArrayList<>();

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private List<VarianteProducto> variantes = new ArrayList<>();

    public Producto() {
    }
}
