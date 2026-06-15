package pe.com.gamarra360.backend.usuario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.gamarra360.backend.security.JwtService;
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.exception.GlobalExceptionHandler;
import pe.com.gamarra360.backend.usuario.dto.AuthResponse;
import pe.com.gamarra360.backend.usuario.dto.LoginRequest;
import pe.com.gamarra360.backend.usuario.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false) // Desactivamos filtros de seguridad para centrar la prueba en el controlador
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setContrasenha("password123");

        authResponse = new AuthResponse("fake-jwt-token", 1, "test@example.com", "CLIENTE");
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Retorna 200 y el token si el login es exitoso")
    void login_Success() throws Exception {
        // GIVEN: El servicio de autenticación retorna una respuesta válida
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // WHEN & THEN: Se realiza la petición POST y se valida el cuerpo de la respuesta
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Retorna 400 si el request es inválido (Email vacío)")
    void login_BadRequest_MissingEmail() throws Exception {
        // GIVEN: Un request con email vacío
        loginRequest.setEmail("");

        // WHEN & THEN: Se espera un error 400 debido a las validaciones @NotBlank
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}
