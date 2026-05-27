package pe.com.gamarra360.backend.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.admin.dto.*;
import pe.com.gamarra360.backend.admin.repository.AdminComercianteRepository;
import pe.com.gamarra360.backend.admin.repository.AdminTiendaRepository;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AdminVendorService
 *
 * Capa de negocio para CU-04 (RF-14).
 *
 * Implementa la máquina de estados del Comerciante y orquesta los efectos
 * secundarios de cada transición de estado (persistencia, notificaciones, auditoría).
 *
 * Cada método público que cambia estado está envuelto en @Transactional para
 * garantizar atomicidad: o todos los pasos se completan, o ninguno.
 * Las notificaciones asíncronas se disparan al commit exitoso
 * mediante @TransactionalEventListener(phase = AFTER_COMMIT).
 */
@Service
@RequiredArgsConstructor
public class AdminVendorService {

    private final AdminComercianteRepository comercianteRepository;
    private final AdminTiendaRepository tiendaRepository;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Cola de solicitudes (lectura)
    // -------------------------------------------------------------------------

    /**
     * Lista comerciantes en estado PENDIENTE_APROBACION, ordenados por fecha
     * de solicitud ascendente (FIFO: el más antiguo primero).
     */
    @Transactional(readOnly = true)
    public Page<SolicitudVendedorDTO> listarPendientes(Pageable pageable) {
        return comercianteRepository.findByEstado("PENDIENTE_APROBACION", pageable)
                .map(this::mapearSolicitud);
    }

    /**
     * Conteo de solicitudes pendientes para el badge del panel de administración.
     */
    @Transactional(readOnly = true)
    public ConteoDTO contarPendientes() {
        long total = comercianteRepository.countByEstado("PENDIENTE_APROBACION");
        return new ConteoDTO(total);
    }

    /**
     * Detalle completo de la solicitud de un comerciante.
     */
    @Transactional(readOnly = true)
    public SolicitudVendedorDetalleDTO obtenerDetalleSolicitud(Integer comercianteId) {
        var comerciante = comercianteRepository.findByIdConUsuario(comercianteId)
                .orElseThrow();
        return mapearDetalle(comerciante);
    }

    /**
     * Lista todos los comerciantes con filtro opcional por estado.
     */
    @Transactional(readOnly = true)
    public Page<SolicitudVendedorDTO> listarTodos(String estado, Pageable pageable) {
        if (estado != null && !estado.isBlank()) {
            return comercianteRepository.findByEstado(estado, pageable)
                    .map(this::mapearSolicitud);
        }
        return comercianteRepository.findAll(pageable).map(this::mapearSolicitud);
    }

    // -------------------------------------------------------------------------
    // Transiciones de estado
    // -------------------------------------------------------------------------

    /**
     * Aprueba formalmente la solicitud de un comerciante.
     *
     * Pasos atómicos dentro de la transacción:
     *  1. Valida que el comerciante esté en estado PENDIENTE_APROBACION.
     *  2. Actualiza Comerciante: verificado=true, activo=true.
     *  3. Crea o activa la Tienda asociada (asigna tenant_id = idTienda).
     *  4. Publica VendedorAprobadoEvent → notificación asíncrona post-commit.
     *  5. Registra en log de auditoría.
     */
    @Transactional
    public RespuestaAprobacionDTO aprobarVendedor(Integer comercianteId) {
        var comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow();

        validarTransicion(comerciante.getEstado(), "PENDIENTE_APROBACION",
                "Solo se pueden aprobar solicitudes en estado PENDIENTE_APROBACION.");

        // 1. Actualizar estado del comerciante
        comerciante.setVerificado(true);
        comerciante.setActivo(true);
        comerciante.setEstado("APROBADO");
        comercianteRepository.save(comerciante);

        // 2. Activar la Tienda (crea si no existe, activa si ya existe)
        var tienda = tiendaRepository.findByIdComerciante(comercianteId)
                .orElseGet(() -> crearTienda(comerciante));
        tienda.setActiva(true);
        tiendaRepository.save(tienda);

        // 3. Publicar evento de dominio (notificación + auditoría post-commit)
        eventPublisher.publishEvent(new VendedorAprobadoEvent(comerciante));

        return new RespuestaAprobacionDTO(
                comercianteId, "APROBADO",
                "Vendedor aprobado. La tienda ha sido habilitada en la plataforma."
        );
    }

    /**
     * Rechaza la solicitud de un comerciante.
     *
     * El motivo queda registrado en el sistema y es comunicado al comerciante.
     * El usuario puede re-solicitar su registro (flujo del frontend).
     */
    @Transactional
    public RespuestaAprobacionDTO rechazarVendedor(Integer comercianteId, String razon) {
        var comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ComercianteNoEncontradoException(comercianteId));

        validarTransicion(comerciante.getEstado(), "PENDIENTE_APROBACION",
                "Solo se pueden rechazar solicitudes en estado PENDIENTE_APROBACION.");

        comerciante.setVerificado(false);
        comerciante.setActivo(false);
        comerciante.setEstado("RECHAZADO");
        comerciante.setMotivoRechazo(razon);
        comercianteRepository.save(comerciante);

        eventPublisher.publishEvent(new VendedorRechazadoEvent(comerciante, razon));

        return new RespuestaAprobacionDTO(
                comercianteId, "RECHAZADO",
                "Solicitud rechazada. Se ha notificado al comerciante."
        );
    }

    /**
     * Suspende temporalmente a un comerciante aprobado.
     *
     * Sus productos quedan inactivos en el catálogo (no visibles para clientes).
     * El historial de pedidos y datos permanecen intactos.
     */
    @Transactional
    public RespuestaAprobacionDTO suspenderVendedor(Integer comercianteId, String razon) {
        var comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ComercianteNoEncontradoException(comercianteId));

        validarTransicion(comerciante.getEstado(), "APROBADO",
                "Solo se pueden suspender comerciantes en estado APROBADO.");

        comerciante.setActivo(false);
        comerciante.setEstado("SUSPENDIDO");
        comerciante.setMotivoRechazo(razon);
        comercianteRepository.save(comerciante);

        // Desactiva la tienda y sus productos (en cascada, manejado en el repositorio)
        tiendaRepository.desactivarTiendaYProductos(comercianteId);

        eventPublisher.publishEvent(new VendedorSuspendidoEvent(comerciante, razon));

        return new RespuestaAprobacionDTO(
                comercianteId, "SUSPENDIDO",
                "Comerciante suspendido. La tienda y sus productos han sido ocultados."
        );
    }

    // -------------------------------------------------------------------------
    // Helpers internos
    // -------------------------------------------------------------------------

    private void validarTransicion(String estadoActual, String estadoRequerido, String mensaje) {
        if (!estadoRequerido.equals(estadoActual)) {
            throw new TransicionEstadoInvalidaException(mensaje);
        }
    }

    private Tienda crearTienda(Comerciante comerciante) {
        Tienda tienda = new Tienda();
        tienda.setIdComerciante(comerciante.getUsuarioId());
        tienda.setNombreComercial(comerciante.getRazonSocial());
        tienda.setVerificada(true);
        tienda.setActiva(true);
        return tienda;
    }

    private SolicitudVendedorDTO mapearSolicitud(Comerciante comerciante) {
        if (comerciante == null) {
            return null;
        }
        SolicitudVendedorDTO dto = new SolicitudVendedorDTO();
        dto.setComercianteId(comerciante.getUsuarioId());
        dto.setRuc(comerciante.getRuc());
        dto.setRazonSocial(comerciante.getRazonSocial());
        dto.setEmailContacto(comerciante.getEmail());
        dto.setEstado(comerciante.getEstado());
        dto.setFechaSolicitud(LocalDateTime.now());
        
        if (comerciante.getTienda() != null) {
            dto.setNombreComercial(comerciante.getTienda().getNombreComercial());
        }
        return dto;
    }

    private SolicitudVendedorDetalleDTO mapearDetalle(Comerciante comerciante) {
        if (comerciante == null) {
            return null;
        }
        SolicitudVendedorDetalleDTO dto = new SolicitudVendedorDetalleDTO();
        dto.setComercianteId(comerciante.getUsuarioId());
        dto.setRuc(comerciante.getRuc());
        dto.setRazonSocial(comerciante.getRazonSocial());
        dto.setEstado(comerciante.getEstado());
        dto.setMotivoRechazo(comerciante.getMotivoRechazo());
        dto.setFechaSolicitud(LocalDateTime.now());

        if (comerciante.getTienda() != null) {
            dto.setNombreComercial(comerciante.getTienda().getNombreComercial());
            dto.setInformacion(comerciante.getTienda().getInformacion());
            dto.setFotoUrl(comerciante.getTienda().getFoto());
        }

        UsuarioResumenDTO userDto = new UsuarioResumenDTO();
        userDto.setUsuarioId(comerciante.getUsuarioId());
        String nombres = comerciante.getNombres() != null ? comerciante.getNombres() : "";
        String apellido = comerciante.getPrimerApellido() != null ? comerciante.getPrimerApellido() : "";
        userDto.setNombreCompleto((nombres + " " + apellido).trim());
        userDto.setEmail(comerciante.getEmail());
        userDto.setRol(comerciante.getRol() != null ? comerciante.getRol().name() : null);
        userDto.setActivo(Boolean.TRUE.equals(comerciante.getActivo()));
        userDto.setFechaRegistro(LocalDateTime.now());
        dto.setUsuario(userDto);

        dto.setDocumentosUrl(List.of());

        return dto;
    }
}
