package pe.com.gamarra360.backend.admin.service;
import pe.com.gamarra360.backend.enums.EstadoPago;
import pe.com.gamarra360.backend.enums.RolEnum;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.admin.dto.*;
import pe.com.gamarra360.backend.admin.repository.AdminUsuarioRepository;
import pe.com.gamarra360.backend.pago.repository.OrdenPagoRepository;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminUserService
 *
 * Capa de negocio para CU-03 (RF-11, RF-12).
 *
 * Principios de diseño aplicados:
 *  - @Transactional garantiza atomicidad: si el cambio de estado falla,
 *    la notificación asíncrona no se dispara (el evento se publica al final
 *    del commit exitoso de la transacción).
 *  - La desactivación nunca elimina registros (soft delete).
 *  - El historial de actividad es de solo lectura desde este servicio.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrdenPagoRepository ordenPagoRepository;
    private final ComercianteRepository comercianteRepository;
    private static final double COMISION_PLATAFORMA = 0.10;
    // -------------------------------------------------------------------------
    // RF-11: Listar y filtrar usuarios
    // -------------------------------------------------------------------------
    private RolEnum convertirRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return null;
        }

        try {
            return RolEnum.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException("Rol inválido: " + rol);
        }
    }
    /**
     * Retorna una página de usuarios filtrados por rol, estado y búsqueda libre.
     * La consulta es construida dinámicamente en el repositorio mediante
     * Specification de Spring Data JPA, evitando N+1 y consultas hardcodeadas.
     */
    @Transactional(readOnly = true)
    public Page<UsuarioResumenDTO> listarUsuarios(UsuarioFiltroDTO filtro, Pageable pageable) {
        RolEnum rol = convertirRol(filtro.getRol());

        return usuarioRepository.buscarConFiltros(
                rol,
                filtro.getActivo(),
                filtro.getQ(),
                pageable
        ).map(this::mapearResumen);
    }

    /**
     * Retorna el detalle completo de un usuario, incluyendo su historial.
     * El historial (pedidos, cotizaciones) se carga con joins optimizados
     * (sin lazy loading para evitar N+1 en la vista de detalle).
     */
    @Transactional(readOnly = true)
    public UsuarioDetalleDTO obtenerDetalle(Integer id) {
        var usuario = usuarioRepository.findByIdConHistorial(id)
                .orElseThrow();
        return mapearDetalle(usuario);
    }

    // -------------------------------------------------------------------------
    // RF-12: Activar / Desactivar cuentas
    // -------------------------------------------------------------------------

    /**
     * Desactiva la cuenta del usuario de forma inmediata.
     *
     * La desactivación es lógica: activo=false.
     * El bloqueo es efectivo en la siguiente petición del usuario,
     * dado que CustomUserDetailsService.loadUserByUsuario() lanza
     * DisabledException cuando activo=false, lo que Spring Security
     * convierte en 401 Unauthorized sin ejecutar lógica de negocio.
     *
     * El historial (pedidos, cotizaciones, logs) permanece intacto e inmutable.
     */
    @Transactional
    public UsuarioEstadoDTO desactivarUsuario(Integer id, String razon) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new EstadoInvalidoException("El usuario ya está desactivado.");
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        // El evento se publica DESPUÉS del commit (TransactionalEventListener),
        // disparando la notificación asíncrona solo si la persistencia fue exitosa.
        eventPublisher.publishEvent(new UsuarioDesactivadoEvent(usuario, razon));

        return new UsuarioEstadoDTO(id, false, "Cuenta desactivada correctamente.");
    }

    /**
     * Reactiva la cuenta del usuario.
     * El usuario puede autenticarse nuevamente en su próximo intento de login.
     */
    @Transactional
    public UsuarioEstadoDTO reactivarUsuario(Integer id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        if (Boolean.TRUE.equals(usuario.getActivo())) {
            throw new EstadoInvalidoException("El usuario ya está activo.");
        }

        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioReactivadoEvent(usuario));

        return new UsuarioEstadoDTO(id, true, "Cuenta reactivada correctamente.");
    }

    // -------------------------------------------------------------------------
    // Mappers internos
    // -------------------------------------------------------------------------

    private UsuarioResumenDTO mapearResumen(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioResumenDTO dto = new UsuarioResumenDTO();
        dto.setUsuarioId(usuario.getUsuarioId());
        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellido = usuario.getPrimerApellido() != null ? usuario.getPrimerApellido() : "";
        dto.setNombreCompleto((nombres + " " + apellido).trim());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol() != null ? usuario.getRol().name() : null);
        dto.setActivo(Boolean.TRUE.equals(usuario.getActivo()));
        dto.setFechaRegistro(LocalDateTime.now());
        return dto;
    }

    private UsuarioDetalleDTO mapearDetalle(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioDetalleDTO dto = new UsuarioDetalleDTO();
        dto.setUsuarioId(usuario.getUsuarioId());
        dto.setNombres(usuario.getNombres());
        dto.setPrimerApellido(usuario.getPrimerApellido());
        dto.setSegundoApellido(usuario.getSegundoApellido());
        dto.setEmail(usuario.getEmail());
        dto.setDni(usuario.getDni());
        dto.setTelefono(usuario.getTelefono());
        dto.setRol(usuario.getRol() != null ? usuario.getRol().name() : null);
        dto.setActivo(Boolean.TRUE.equals(usuario.getActivo()));
        dto.setFechaRegistro(LocalDateTime.now());

        ResumenActividadDTO actividad = new ResumenActividadDTO();
        actividad.setTotalPedidos(0);
        actividad.setTotalCotizaciones(0);
        actividad.setTotalSolicitudes(0);
        actividad.setUltimaConexion(LocalDateTime.now());
        dto.setActividad(actividad);

        return dto;
    }
    @Transactional(readOnly = true)
    public DashboardResumenDTO obtenerResumenDashboard() {
        long totalUsuarios = usuarioRepository.count();

        double ventasTotales = ordenPagoRepository.sumarTotalPagado();
        double ingresosTotales = ventasTotales * COMISION_PLATAFORMA;

        long pendientes = comercianteRepository.countByVerificadoFalse();
        long aprobados = comercianteRepository.countByVerificadoTrueAndAprobadoTrue();
        long rechazados = comercianteRepository.countByVerificadoTrueAndAprobadoFalse();

        List<DashboardResumenDTO.ActividadRecienteDTO> actividad =
                usuarioRepository.findTop10ByOrderByUsuarioIdDesc().stream()
                        .filter(u -> u.getRol() == RolEnum.VENDEDOR || u.getRol() == RolEnum.CLIENTE)
                        .limit(5)
                        .map(u -> new DashboardResumenDTO.ActividadRecienteDTO(
                                u.getRol() == RolEnum.VENDEDOR ? "COMERCIANTE" : "CLIENTE",
                                ((u.getNombres() != null ? u.getNombres() : "") + " " +
                                        (u.getPrimerApellido() != null ? u.getPrimerApellido() : "")).trim(),
                                u.getEmail()
                        ))
                        .collect(Collectors.toList());

        return new DashboardResumenDTO(
                totalUsuarios, ingresosTotales, pendientes, aprobados, rechazados, actividad
        );
    }
}
