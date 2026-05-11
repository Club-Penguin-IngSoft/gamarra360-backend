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
@Table(name = "colores")
@Getter
@Setter
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_color")
    private Integer idColor;
    private String nombre;
    @Column(name = "cod_hex")
    private String codHex;
    private Boolean activo;
    @OneToMany(mappedBy = "colorRef")
    @JsonIgnore
    private List<VarianteProducto> variantes = new ArrayList<>();
    public Color() { }
}
