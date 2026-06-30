package pe.com.gamarra360.backend.usuario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_verificacion")
@Getter
@Setter
@NoArgsConstructor
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email al que se envió el código */
    @Column(nullable = false)
    private String email;

    /** Código de 6 dígitos */
    @Column(nullable = false, length = 6)
    private String codigo;

    /** Cuándo expira (10 minutos desde la creación) */
    @Column(nullable = false)
    private LocalDateTime expiracion;

    /** true una vez que el usuario lo validó correctamente */
    @Column(nullable = false)
    private boolean usado = false;

    public CodigoVerificacion(String email, String codigo, LocalDateTime expiracion) {
        this.email = email;
        this.codigo = codigo;
        this.expiracion = expiracion;
    }

    public boolean estaVigente() {
        return !usado && LocalDateTime.now().isBefore(expiracion);
    }
}
