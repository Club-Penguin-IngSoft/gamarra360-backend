package pe.com.gamarra360.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class Gamarra360Application {

    public static void main(String[] args) {
        // Fuerza JVM a UTC antes de que cualquier componente se inicialice.
        // Sin esto, LocalDateTime se convierte a Timestamp usando el TZ del SO
        // antes de llegar al conector JDBC, causando desfases al persistir fechas.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(Gamarra360Application.class, args);
    }
}
