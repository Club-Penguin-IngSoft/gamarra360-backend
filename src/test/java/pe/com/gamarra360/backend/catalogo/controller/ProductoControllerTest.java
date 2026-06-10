package pe.com.gamarra360.backend.catalogo.controller;

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
import pe.com.gamarra360.backend.catalogo.dto.ImagenRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.service.ProductoService;
import pe.com.gamarra360.backend.exception.GlobalExceptionHandler;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.security.JwtService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ProductoController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductoRequest validRequest;
    private ProductoResponse response;
    private UsernamePasswordAuthenticationToken authSimulado;

    @BeforeEach
    void setUp() {
        validRequest = new ProductoRequest();
        validRequest.setNombre("Polo Oversize");
        validRequest.setDescripcion("Polo de algodón pima");
        validRequest.setPrecioBase(45.0);
        validRequest.setIdCategoria(1);
        validRequest.setIdTipoProducto(1);
        
        ImagenRequest img = new ImagenRequest();
        img.setUrl("http://aws.s3/img.jpg");
        validRequest.setImagenes(List.of(img));

        response = new ProductoResponse();
        response.setIdProducto(1);
        response.setNombre("Polo Oversize");

        // SOLUCIÓN DEFINITIVA: Simulamos el Principal exacto para el Vendedor
        UsuarioPrincipal principalSimulado = Mockito.mock(UsuarioPrincipal.class);
        when(principalSimulado.getUsuarioId()).thenReturn(1);

        authSimulado = new UsernamePasswordAuthenticationToken(
                principalSimulado, null, List.of(new SimpleGrantedAuthority("ROLE_VENDEDOR"))
        );
    }

    @Test
    @DisplayName("POST /api/v1/productos - Debería retornar 201 Created")
    void crear_DeberiaRetornarCreated() throws Exception {
        Mockito.when(service.crearProducto(Mockito.any(ProductoRequest.class), Mockito.any()))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(authSimulado)) // INYECTAMOS EL AUTH MANUALMENTE AQUÍ
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} - Debería retornar 404 cuando no existe")
    void obtener_DeberiaRetornar404_CuandoNoExiste() throws Exception {
        when(service.obtenerProductoResponse(anyInt()))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));

        mockMvc.perform(get("/api/v1/productos/99"))
                // Aquí no es estricto inyectar authSimulado si el GET es público o no extrae el ID,
                // pero si el controlador lo llegase a pedir, solo agrégale .principal(authSimulado)
                .andExpect(status().isNotFound())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof RecursoNoEncontradoException));
    }
}