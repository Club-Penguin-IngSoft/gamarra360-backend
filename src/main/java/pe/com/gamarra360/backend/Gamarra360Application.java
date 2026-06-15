package pe.com.gamarra360.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Gamarra 360°.
 *
 * Arranca el contexto de Spring Boot y todos los módulos del monolito modular:
 *  - catalogo     (CU-07, CU-08) — productos, búsqueda
 *  - usuario      (CU-01 a CU-06) — registro, login, perfiles  [otros equipos]
 *  - cotizacion   (otros equipos)
 *  - personalizacion, carrito, pedido, pago (otros equipos)
 */
@SpringBootApplication
public class Gamarra360Application {

    public static void main(String[] args) {
        SpringApplication.run(Gamarra360Application.class, args);
    }
}
