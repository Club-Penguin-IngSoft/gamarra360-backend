package pe.com.gamarra360.backend.catalogo.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "especificaciones")
@Getter
@Setter
public class Especificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especificacion")
    private Integer idEspecificacion;
    private String nombre;
    private String descripcion;
    @Column(name = "id_producto")
    private Integer idProducto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    @JsonIgnore
    private Producto producto;
    public Especificacion() { }
}
