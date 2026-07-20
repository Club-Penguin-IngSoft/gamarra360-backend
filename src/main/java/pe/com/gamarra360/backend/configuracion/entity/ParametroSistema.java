package pe.com.gamarra360.backend.configuracion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parametros_sistema")
@Getter
@Setter
@NoArgsConstructor
public class ParametroSistema {
    @Id
    @Column(name = "clave", length = 80)
    private String clave;

    @Column(name = "valor", nullable = false, length = 500)
    private String valor;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "editable", nullable = false)
    private Boolean editable = true;
}
