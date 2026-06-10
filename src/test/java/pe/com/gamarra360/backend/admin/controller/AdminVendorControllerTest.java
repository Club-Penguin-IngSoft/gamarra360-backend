package pe.com.gamarra360.backend.admin.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.gamarra360.backend.admin.service.AdminVendorService;
import pe.com.gamarra360.backend.admin.service.ComercianteNoEncontradoException;
import pe.com.gamarra360.backend.exception.GlobalExceptionHandler;
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.security.JwtService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminVendorController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AdminVendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminVendorService adminVendorService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/admin/vendedores/{id} - Debería retornar 404 cuando el comerciante no existe")
    void obtenerSolicitud_DeberiaRetornar404_CuandoNoExiste() throws Exception {
        // GIVEN: El servicio lanza ComercianteNoEncontradoException
        when(adminVendorService.obtenerDetalleSolicitud(anyInt()))
                .thenThrow(new ComercianteNoEncontradoException(-1)); /*COMERCIANTE NO ENCONTRADO */

        // WHEN & THEN: Fix #3 - Capturar la excepción interna y verificar su tipo
        mockMvc.perform(get("/api/v1/admin/vendedores/1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof ComercianteNoEncontradoException));
    }
}
