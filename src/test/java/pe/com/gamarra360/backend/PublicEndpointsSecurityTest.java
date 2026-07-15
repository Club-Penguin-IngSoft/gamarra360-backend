package pe.com.gamarra360.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.gamarra360.backend.catalogo.controller.ProductoController;
import pe.com.gamarra360.backend.catalogo.controller.TiendaController;
import pe.com.gamarra360.backend.catalogo.dto.PerfilTiendaPublicaDto;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.service.ProductoService;
import pe.com.gamarra360.backend.catalogo.service.TiendaService;
import pe.com.gamarra360.backend.config.SecurityConfig;
import pe.com.gamarra360.backend.pedido.service.CheckoutController;
import pe.com.gamarra360.backend.pedido.service.CheckoutService;
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.security.JwtAuthenticationFilter;
import pe.com.gamarra360.backend.security.JwtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductoController.class, TiendaController.class, CheckoutController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PublicEndpointsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private TiendaService tiendaService;

    @MockBean
    private CheckoutService checkoutService;

    // Dependencias de seguridad requeridas para que el contexto levante
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    // ==========================================
    // 1. ENDPOINTS PÚBLICOS (Esperar 200 OK)
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/productos - Permitido sin autenticación")
    void listarProductos_Publico_Retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/productos")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productoService).listarConFiltros(any());
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} - Permitido sin autenticación")
    void obtenerProducto_Publico_Retorna200() throws Exception {
        ProductoResponse mockResponse = new ProductoResponse();
        mockResponse.setIdProducto(1);
        mockResponse.setNombre("Polo de Prueba");

        when(productoService.obtenerProductoResponse(anyInt())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/productos/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productoService).obtenerProductoResponse(1);
    }

    @Test
    @DisplayName("GET /api/v1/tiendas/publico/{id} - Permitido sin autenticación")
    void obtenerPerfilTiendaPublico_Publico_Retorna200() throws Exception {
        PerfilTiendaPublicaDto mockResponse = new PerfilTiendaPublicaDto();

        when(tiendaService.obtenerPerfilPublico(anyInt())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/tiendas/publico/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tiendaService).obtenerPerfilPublico(1);
    }

    // ==========================================
    // 2. ENDPOINTS PROTEGIDOS (Esperar 401 Unauthorized)
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/checkout - Protegido, retorna 403 y no ejecuta lógica de negocio")
    void checkout_Protegido_Retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        // Verificamos que NUNCA se invoca al servicio real
        verify(checkoutService, never()).procesarCompra(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/productos - Protegido, retorna 403 y no ejecuta lógica de negocio")
    void crearProducto_Protegido_Retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        // Verificamos que NUNCA se invoca al servicio real
        verify(productoService, never()).crearProducto(any(), any());
    }
}
