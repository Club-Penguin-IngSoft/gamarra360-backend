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

import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import pe.com.gamarra360.backend.admin.dto.RespuestaAprobacionDTO;
import pe.com.gamarra360.backend.admin.dto.MotivoDTO;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

    @Test
    @DisplayName("POST /api/v1/admin/vendedores/{id}/aprobar - Debería aprobar la solicitud del comerciante (ADM-001)")
    void aprobarVendedor_Success() throws Exception {
        RespuestaAprobacionDTO response = new RespuestaAprobacionDTO(1, "APROBADO", "Comerciante aprobado correctamente");
        when(adminVendorService.aprobarVendedor(1)).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/vendedores/1/aprobar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comercianteId").value(1))
                .andExpect(jsonPath("$.nuevoEstado").value("APROBADO"))
                .andExpect(jsonPath("$.mensaje").value("Comerciante aprobado correctamente"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/vendedores/{id}/rechazar - Debería rechazar la solicitud del comerciante (ADM-002)")
    void rechazarVendedor_Success() throws Exception {
        MotivoDTO motivo = new MotivoDTO("Documento RUC no coincide");
        RespuestaAprobacionDTO response = new RespuestaAprobacionDTO(1, "RECHAZADO", "Comerciante rechazado correctamente");
        when(adminVendorService.rechazarVendedor(1, "Documento RUC no coincide")).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/vendedores/1/rechazar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(motivo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comercianteId").value(1))
                .andExpect(jsonPath("$.nuevoEstado").value("RECHAZADO"));
    }
}
