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
     * Lista comerciantes pendientes: verificado = false (0).
     */
    @Transactional(readOnly = true)
    public Page<SolicitudVendedorDTO> listarPendientes(Pageable pageable) {
        return comercianteRepository.findByVerificado(false, pageable)
                .map(this::mapearSolicitud);
    }

    /**
     * Conteo ignorado por ahora — devuelve 0.
     */
    @Transactional(readOnly = true)
    public ConteoDTO contarPendientes() {
        return new ConteoDTO(0);
    }

    /**
     * Detalle ignorado por ahora.
     */
    @Transactional(readOnly = true)
    public SolicitudVendedorDetalleDTO obtenerDetalleSolicitud(Integer comercianteId) {
        var comerciante = comercianteRepository.findByIdConUsuario(comercianteId)
                .orElseThrow();
        return mapearDetalle(comerciante);
    }

    /**
     * Lista todos los comerciantes con filtro opcional por estado.
     * PENDIENTE  → verificado = false
     * APROBADO   → verificado = true,  activo = true
     * RECHAZADO  → verificado = false, activo = false  (ya cubierto por verificado=false)
     * SUSPENDIDO → verificado = true,  activo = false
     */
    @Transactional(readOnly = true)
    public Page<SolicitudVendedorDTO> listarTodos(String estado, Pageable pageable) {
        if (estado != null && !estado.isBlank()) {
            return switch (estado.toUpperCase()) {
                case "APROBADO"  -> comercianteRepository
                        .findByVerificadoAndAprobado(true, true, pageable)
                        .map(this::mapearSolicitud);
                case "RECHAZADO" -> comercianteRepository
                        .findByVerificadoAndAprobado(true, false, pageable)
                        .map(this::mapearSolicitud);
                default          -> comercianteRepository
                        .findByVerificado(false, pageable)
                        .map(this::mapearSolicitud); // PENDIENTE
            };
        }
        return comercianteRepository.findAll(pageable).map(this::mapearSolicitud);
    }

    // -------------------------------------------------------------------------
    // Transiciones de estado
    // -------------------------------------------------------------------------

    /**
     * Aprobar: verificado = 1, activo = 1.
     * Activa o crea la tienda asociada.
     */
    @Transactional
    public RespuestaAprobacionDTO aprobarVendedor(Integer comercianteId) {
        var comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ComercianteNoEncontradoException(comercianteId));

        comerciante.setVerificado(true);  // verificado = 1
        comerciante.setAprobado(true);    // aprobado = 1
        comercianteRepository.save(comerciante);
        // Crear tienda si no existe
        tiendaRepository.findByIdComerciante(comercianteId).orElseGet(() -> tiendaRepository.save(crearTienda(comerciante)));
        eventPublisher.publishEvent(new VendedorAprobadoEvent(comerciante));

        return new RespuestaAprobacionDTO(
                comercianteId, "APROBADO",
                "Vendedor aprobado correctamente."
        );
    }

    /**
     * Rechazar: verificado = 0 (se mantiene), activo = 0.
     */
    @Transactional
    public RespuestaAprobacionDTO rechazarVendedor(Integer comercianteId, String razon) {
        var comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ComercianteNoEncontradoException(comercianteId));

        comerciante.setVerificado(true);  // verificado = 1
        comerciante.setAprobado(false);   // aprobado = 0
        comercianteRepository.save(comerciante);

        eventPublisher.publishEvent(new VendedorRechazadoEvent(comerciante, razon));

        return new RespuestaAprobacionDTO(
                comercianteId, "RECHAZADO",
                "Solicitud rechazada. Se ha notificado al comerciante."
        );
    }

    /**
     * Suspender: verificado = 1 (se mantiene), activo = 0.
     */
//    @Transactional
//    public RespuestaAprobacionDTO suspenderVendedor(Integer comercianteId, String razon) {
//        var comerciante = comercianteRepository.findById(comercianteId)
//                .orElseThrow(() -> new ComercianteNoEncontradoException(comercianteId));
//
//        comerciante.setActivo(false);      // verificado sigue en 1
//        comercianteRepository.save(comerciante);
//
//        tiendaRepository.desactivarTiendaYProductos(comercianteId);
//
//        eventPublisher.publishEvent(new VendedorSuspendidoEvent(comerciante, razon));
//
//        return new RespuestaAprobacionDTO(
//                comercianteId, "SUSPENDIDO",
//                "Comerciante suspendido. La tienda y sus productos han sido ocultados."
//        );
//    }

    // -------------------------------------------------------------------------
    // Helpers internos
    // -------------------------------------------------------------------------

    /** Mapea el string de estado al valor de verificado correspondiente. */
    private Boolean resolverVerificado(String estado) {
        return switch (estado.toUpperCase()) {
            case "APROBADO", "SUSPENDIDO" -> true;
            default -> false; // PENDIENTE, RECHAZADO
        };
    }

    /** Mapea el string de estado al valor de activo correspondiente. */
    private Boolean resolverActivo(String estado) {
        return switch (estado.toUpperCase()) {
            case "APROBADO" -> true;
            default -> false; // RECHAZADO, SUSPENDIDO, PENDIENTE
        };
    }

    // crearTienda() — sin activa
    private Tienda crearTienda(Comerciante comerciante) {
        Tienda tienda = new Tienda();
        tienda.setIdComerciante(comerciante.getUsuarioId());
        tienda.setNombreComercial(comerciante.getNombreTienda()); // nombre_tienda de comerciantes
        tienda.setInformacion(null);
        tienda.setFoto(comerciante.getLogoUrl());
        tienda.setVerificada(true);
        //tienda.setGaleria(null);//Falta implementar
        //tienda.setPiso(null);
        //tienda.setStand(null);
        return tienda;
    }

    private SolicitudVendedorDTO mapearSolicitud(Comerciante comerciante) {
        if (comerciante == null) return null;

        SolicitudVendedorDTO dto = new SolicitudVendedorDTO();
        dto.setComercianteId(comerciante.getUsuarioId());
        dto.setRuc(comerciante.getRuc());
        dto.setRazonSocial(comerciante.getRazonSocial());
        dto.setEmailContacto(comerciante.getEmail());
        dto.setFechaSolicitud(LocalDateTime.now());
        dto.setNombreTienda(comerciante.getNombreTienda());
        if (comerciante.getTienda() != null) {
            dto.setNombreComercial(comerciante.getTienda().getNombreComercial());
        }
        return dto;
    }

    private SolicitudVendedorDetalleDTO mapearDetalle(Comerciante comerciante) {
        if (comerciante == null) return null;

        SolicitudVendedorDetalleDTO dto = new SolicitudVendedorDetalleDTO();
        dto.setComercianteId(comerciante.getUsuarioId());
        dto.setRuc(comerciante.getRuc());
        dto.setRazonSocial(comerciante.getRazonSocial());
        dto.setFechaSolicitud(LocalDateTime.now());

        if (comerciante.getTienda() != null) {
            dto.setNombreComercial(comerciante.getTienda().getNombreComercial());
            dto.setInformacion(comerciante.getTienda().getInformacion());
            dto.setFotoUrl(comerciante.getTienda().getFoto());
        }

        UsuarioResumenDTO userDto = new UsuarioResumenDTO();
        userDto.setUsuarioId(comerciante.getUsuarioId());
        String nombres  = comerciante.getNombres()        != null ? comerciante.getNombres()        : "";
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