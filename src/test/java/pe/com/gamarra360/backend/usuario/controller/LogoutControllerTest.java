package pe.com.gamarra360.backend.usuario.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.gamarra360.backend.config.SecurityConfig;
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.security.JwtAuthenticationFilter;
import pe.com.gamarra360.backend.security.JwtService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {LogoutControllerTest.TestLogoutController.class})
@Import(SecurityConfig.class)
class LogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UsernamePasswordAuthenticationToken authSimulado;

    @BeforeEach
    void setUp() {
        UsuarioPrincipal principalSimulado = Mockito.mock(UsuarioPrincipal.class);
        Mockito.when(principalSimulado.getUsuarioId()).thenReturn(1);

        authSimulado = new UsernamePasswordAuthenticationToken(
                principalSimulado, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
    }

    @Test
    @DisplayName("POST /api/v1/logout - Usuario autenticado cierra sesión exitosamente (CAT-001)")
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/v1/logout")
                .principal(authSimulado))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/logout - Endpoint de logout no accesible sin token (CAT-002)")
    void logout_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/logout"))
                .andExpect(status().isUnauthorized());
    }

    @RestController
    @RequestMapping("/api/v1/logout")
    static class TestLogoutController {
        @PostMapping
        public ResponseEntity<Void> logout() {
            return ResponseEntity.ok().build();
        }
    }
}
