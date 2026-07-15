package pe.com.gamarra360.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SmokeTest extends IntegrationTestBase {

    @Test
    @DisplayName("Verificar que el contexto de Spring Boot se inicialice correctamente con base de datos H2")
    void contextLoads() {
        // El test pasa si el contexto de Spring levanta y mockMvc no es nulo
        assertNotNull(mockMvc, "El bean MockMvc debería haber sido inyectado correctamente");
    }
}
