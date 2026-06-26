package pe.com.gamarra360.backend.admin.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.gamarra360.backend.admin.service.AdminUserService;
import pe.com.gamarra360.backend.admin.service.UsuarioNoEncontradoException;
import pe.com.gamarra360.backend.exception.GlobalExceptionHandler;
import pe.com.gamarra360.backend.security.CustomUserDetailsService;
import pe.com.gamarra360.backend.security.JwtService;

import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import pe.com.gamarra360.backend.admin.dto.UsuarioEstadoDTO;
import pe.com.gamarra360.backend.admin.dto.UsuarioResumenDTO;
import pe.com.gamarra360.backend.admin.dto.MotivoDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = {AdminUserController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/admin/usuarios/{id} - Debería retornar 404 cuando el usuario no existe")
    void obtenerUsuario_DeberiaRetornar404_CuandoNoExiste() throws Exception {
        // GIVEN: El servicio lanza UsuarioNoEncontradoException
        when(adminUserService.obtenerDetalle(anyInt()))
                .thenThrow(new UsuarioNoEncontradoException(-1)); /*USUARIO NO ENCONTRADO */

        // WHEN & THEN: Fix #3 - Capturar la excepción interna y verificar su tipo
        mockMvc.perform(get("/api/v1/admin/usuarios/1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof UsuarioNoEncontradoException));
    }

    @Test
    @DisplayName("GET /api/v1/admin/usuarios - Debería retornar listado de usuarios con filtros (USR-001)")
    void listarUsuarios_Success() throws Exception {
        when(adminUserService.listarUsuarios(any(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/v1/admin/usuarios")
                .param("rol", "VENDEDOR")
                .param("activo", "true")
                .param("q", "Juan"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/usuarios/{id}/desactivar - Debería desactivar el usuario (USR-002)")
    void desactivarUsuario_Success() throws Exception {
        MotivoDTO motivo = new MotivoDTO("Incumplimiento de términos");
        UsuarioEstadoDTO response = new UsuarioEstadoDTO(1, false, "Usuario desactivado correctamente");
        when(adminUserService.desactivarUsuario(1, "Incumplimiento de términos")).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/usuarios/1/desactivar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(motivo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.activo").value(false));
    }
}
