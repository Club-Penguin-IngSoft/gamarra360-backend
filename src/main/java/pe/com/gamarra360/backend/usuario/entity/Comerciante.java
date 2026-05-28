package pe.com.gamarra360.backend.usuario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla `comerciantes`. Especialización de Usuario (herencia JOINED por usuario_id).
 * Solo los comerciantes con `verificado = true` pueden mostrar productos
 * en el catálogo público (CU-07, RF-20).
 */
@Entity
@Table(name = "comerciantes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
public class Comerciante extends Usuario {

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "ruc")
    private String ruc;

    @Column(name = "verificado")
    private Boolean verificado;

    /** FK de la tienda asociada (columna id_tienda en la tabla comerciantes). */
    @Column(name = "id_tienda")
    private Long idTienda;
}
