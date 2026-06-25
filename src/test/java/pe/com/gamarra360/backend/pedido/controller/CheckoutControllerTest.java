package pe.com.gamarra360.backend.pedido.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.gamarra360.backend.exception.GlobalExceptionHandler;
import pe.com.gamarra360.backend.pedido.entity.PedidoRequestDTO;
import pe.com.gamarra360.backend.pedido.entity.PedidoResponseDTO;
import pe.com.gamarra360.backend.pedido.service.CheckoutController;
import pe.com.gamarra360.backend.pedido.service.CheckoutService;
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.security.JwtService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CheckoutController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckoutService checkoutService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private PedidoRequestDTO validRequest;
    private UsernamePasswordAuthenticationToken authSimulado;

    @BeforeEach
    void setUp() {
        validRequest = new PedidoRequestDTO(
                1,
                "DELIVERY",
                "Av. Larco 123",
                1,           // idDistrito
                100.0,
                List.of(new PedidoRequestDTO.ItemDTO(1, 2, 50.0))
        );

        // SOLUCIÓN DEFINITIVA: Simulamos el Principal exacto que tu controlador intenta castear
        UsuarioPrincipal principalSimulado = Mockito.mock(UsuarioPrincipal.class);
        when(principalSimulado.getUsuarioId()).thenReturn(1);

        authSimulado = new UsernamePasswordAuthenticationToken(
                principalSimulado, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
    }

    @Test
    @DisplayName("POST /api/v1/checkout - Debería retornar 201 Created")
    void checkout_DeberiaRetornarCreated() throws Exception {
        PedidoResponseDTO mockResponse = new PedidoResponseDTO(
                1L, 10L, "PENDIENTE_CONFIRMACION", "PAGADO",
                110.0, 10.0, "Miraflores", "Lima", "2026-06-26"
        );
        
        when(checkoutService.procesarCompra(any(PedidoRequestDTO.class), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(authSimulado)) // INYECTAMOS EL AUTH MANUALMENTE AQUÍ
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/checkout - Debería retornar 500 cuando ocurre un error inesperado")
    void checkout_DeberiaRetornar500_CuandoFalla() throws Exception {
        when(checkoutService.procesarCompra(any(PedidoRequestDTO.class), any()))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/api/v1/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(authSimulado)) // INYECTAMOS EL AUTH MANUALMENTE AQUÍ
                .andExpect(status().isInternalServerError())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof RuntimeException));
    }
}