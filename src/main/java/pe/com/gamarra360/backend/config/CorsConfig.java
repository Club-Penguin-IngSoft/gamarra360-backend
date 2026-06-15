package pe.com.gamarra360.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuración global de CORS.
 *
 * Expone {@code CorsConfigurationSource} como bean para que Spring Security
 * lo aplique en su cadena de filtros ANTES de que los preflight OPTIONS sean
 * rechazados. El {@link WebMvcConfigurer} cubre el resto de la capa MVC.
 *
 * CLAUDE.md §5: NUNCA usar {@code @CrossOrigin("*")} en producción.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:3000",
            "https://gamarra360.amplifyapp.com",
            "https://gamarra360.duckdns.org"
    );

    private static final List<String> ALLOWED_METHODS =
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /** Bean que Spring Security consume con {@code .cors(Customizer.withDefaults())}. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Mantiene CORS para rutas MVC no cubiertas por Security. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS.toArray(String[]::new))
                .allowedMethods(ALLOWED_METHODS.toArray(String[]::new))
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
