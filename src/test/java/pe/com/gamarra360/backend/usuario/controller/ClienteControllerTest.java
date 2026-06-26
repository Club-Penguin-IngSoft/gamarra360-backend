package pe.com.gamarra360.backend.usuario.controller;

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
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.security.JwtService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import pe.com.gamarra360.backend.usuario.dto.ActualizarDatosPersonalesDto;
import pe.com.gamarra360.backend.usuario.service.ClienteService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ClienteController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ActualizarDatosPersonalesDto dtoValido;
    private UsernamePasswordAuthenticationToken authSimulado;

    @BeforeEach
    void setUp() {
        dtoValido = new ActualizarDatosPersonalesDto("Juan", "Perez", "Gomez", "987654321");

        UsuarioPrincipal principalSimulado = Mockito.mock(UsuarioPrincipal.class);
        when(principalSimulado.getUsuarioId()).thenReturn(1);

        authSimulado = new UsernamePasswordAuthenticationToken(
                principalSimulado, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/perfil/datos-personales - Retorna 204 si la actualización es exitosa (CAT-001)")
    void actualizarDatosPersonales_Success() throws Exception {
        doNothing().when(service).actualizarDatosPersonales(Mockito.eq(1), any(ActualizarDatosPersonalesDto.class));

        mockMvc.perform(put("/api/v1/clientes/perfil/datos-personales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoValido))
                .principal(authSimulado))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/perfil/datos-personales - Retorna 400 si el request es inválido (Nombre vacío) (CAT-002)")
    void actualizarDatosPersonales_BadRequest() throws Exception {
        ActualizarDatosPersonalesDto dtoInvalido = new ActualizarDatosPersonalesDto("", "Perez", "Gomez", "987654321");

        mockMvc.perform(put("/api/v1/clientes/perfil/datos-personales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoInvalido))
                .principal(authSimulado))
                .andExpect(status().isBadRequest());
    }
}
