package pe.com.gamarra360.backend.catalogo.entity;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "tallas")
@Getter
@Setter
public class Talla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_talla")
    private Integer idTalla;
    private String talla;
    private Boolean activo;
    @OneToMany(mappedBy = "tallaRef")
    @JsonIgnore
    private List<VarianteProducto> variantes = new ArrayList<>();
    public Talla() { }
}
