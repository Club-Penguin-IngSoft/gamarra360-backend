package pe.com.gamarra360.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class Gamarra360Application {

    public static void main(String[] args) {
        // Usa la zona horaria de Lima antes de inicializar los componentes.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        SpringApplication.run(Gamarra360Application.class, args);
    }
}
