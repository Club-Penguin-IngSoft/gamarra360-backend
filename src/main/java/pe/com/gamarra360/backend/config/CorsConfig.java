package com.gamarra360.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS.
 *
 * Permite peticiones desde el frontend React (Vite dev server en :5173 y
 * AWS Amplify en producción). CLAUDE.md §5: NUNCA usar `@CrossOrigin("*")`
 * en producción — siempre restringir a dominios específicos.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        // Dev local (Vite)
                        "http://localhost:5173",
                        "http://localhost:3000",
                        // TODO: agregar dominio de AWS Amplify cuando esté listo
                        "https://gamarra360.amplifyapp.com"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
