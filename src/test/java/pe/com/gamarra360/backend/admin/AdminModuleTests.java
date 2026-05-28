package pe.com.gamarra360.backend.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.admin.dto.*;
import pe.com.gamarra360.backend.admin.service.AdminUserService;
import pe.com.gamarra360.backend.admin.service.AdminVendorService;
import pe.com.gamarra360.backend.admin.repository.AdminComercianteRepository;
import pe.com.gamarra360.backend.admin.repository.AdminUsuarioRepository;
import pe.com.gamarra360.backend.enums.RolEnum;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.entity.Usuario;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_admin;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AdminModuleTests {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminVendorService adminVendorService;

    @Autowired
    private AdminUsuarioRepository usuarioRepository;

    @Autowired
    private AdminComercianteRepository comercianteRepository;

    @Test
    void testListarYGestionarUsuarios() {
        // 1. Create a mock user
        Usuario usuario = new Usuario();
        usuario.setNombres("Juan");
        usuario.setPrimerApellido("Perez");
        usuario.setEmail("juan.perez@example.com");
        usuario.setContrasenha("password");
        usuario.setRol(RolEnum.CLIENTE);
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // 2. Test search with filters
        UsuarioFiltroDTO filtro = new UsuarioFiltroDTO("CLIENTE", true, "Juan");
        Page<UsuarioResumenDTO> page = adminUserService.listarUsuarios(filtro, PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getEmail()).isEqualTo("juan.perez@example.com");

        // 3. Test detailed retrieval
        UsuarioDetalleDTO detalle = adminUserService.obtenerDetalle(usuario.getUsuarioId());
        assertThat(detalle).isNotNull();
        assertThat(detalle.getNombres()).isEqualTo("Juan");

        // 4. Test deactivation
        UsuarioEstadoDTO estado = adminUserService.desactivarUsuario(usuario.getUsuarioId(), "Motivo de prueba");
        assertThat(estado.isActivo()).isFalse();

        // 5. Test reactivation
        UsuarioEstadoDTO estadoReact = adminUserService.reactivarUsuario(usuario.getUsuarioId());
        assertThat(estadoReact.isActivo()).isTrue();
    }

    @Test
    void testAprobarYRechazarVendedor() {
        // 1. Create a mock merchant (Comerciante)
        Comerciante comerciante = new Comerciante();
        comerciante.setNombres("Maria");
        comerciante.setPrimerApellido("Gomez");
        comerciante.setEmail("maria.gomez@example.com");
        comerciante.setContrasenha("password");
        comerciante.setRol(RolEnum.VENDEDOR);
        comerciante.setActivo(false);
        comerciante.setRuc("10123456789");
        comerciante.setRazonSocial("Maria Gomez E.I.R.L.");
        comerciante.setEstado("PENDIENTE_APROBACION");
        comerciante = comercianteRepository.save(comerciante);

        // 2. Test list pending merchants
        Page<SolicitudVendedorDTO> pendientes = adminVendorService.listarPendientes(PageRequest.of(0, 10));
        assertThat(pendientes.getContent()).isNotEmpty();
        
        // 3. Test counter of pending merchant requests
        ConteoDTO conteo = adminVendorService.contarPendientes();
        assertThat(conteo.getTotal()).isGreaterThanOrEqualTo(1);

        // 4. Test details of merchant request
        SolicitudVendedorDetalleDTO detalle = adminVendorService.obtenerDetalleSolicitud(comerciante.getUsuarioId());
        assertThat(detalle).isNotNull();
        assertThat(detalle.getRuc()).isEqualTo("10123456789");

        // 5. Test vendor approval
        RespuestaAprobacionDTO respuestaAprobacion = adminVendorService.aprobarVendedor(comerciante.getUsuarioId());
        assertThat(respuestaAprobacion.getNuevoEstado()).isEqualTo("APROBADO");

        // Reload to verify state change
        Comerciante reloaded = comercianteRepository.findById(comerciante.getUsuarioId()).orElseThrow();
        assertThat(reloaded.getEstado()).isEqualTo("APROBADO");
        assertThat(reloaded.getVerificado()).isTrue();
        assertThat(reloaded.getActivo()).isTrue();

        // 6. Test vendor suspension
        RespuestaAprobacionDTO respuestaSuspension = adminVendorService.suspenderVendedor(comerciante.getUsuarioId(), "Sospecha de fraude");
        assertThat(respuestaSuspension.getNuevoEstado()).isEqualTo("SUSPENDIDO");

        Comerciante reloadedSuspended = comercianteRepository.findById(comerciante.getUsuarioId()).orElseThrow();
        assertThat(reloadedSuspended.getEstado()).isEqualTo("SUSPENDIDO");
        assertThat(reloadedSuspended.getActivo()).isFalse();
    }
}
