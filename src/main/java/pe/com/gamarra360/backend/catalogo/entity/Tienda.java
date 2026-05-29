package pe.com.gamarra360.backend.catalogo.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "tiendas")
@Getter
@Setter
public class Tienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tienda")
    private Integer idTienda;
    @Column(name = "id_comerciante")
    private Integer idComerciante;
    @Column(name = "nombre_comercial")
    private String nombreComercial;
    private String informacion;
    private String foto;
    private Boolean verificada;
    //private Boolean activa;//no se usa

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comerciante", insertable = false, updatable = false)
    @JsonIgnore
    private Comerciante comerciante;

    @OneToMany(mappedBy = "tienda")
    @JsonIgnore
    private List<Producto> productos = new ArrayList<>();

    public Tienda() {
    }
}
