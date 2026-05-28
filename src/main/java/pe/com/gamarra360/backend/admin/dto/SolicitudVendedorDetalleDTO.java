package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista detallada de la solicitud para la pantalla de revisión del administrador.
 * Incluye datos completos del comerciante y su usuario asociado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudVendedorDetalleDTO {
    private Integer comercianteId;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String informacion;
    private String fotoUrl;
    private String estado;
    private String motivoRechazo;    // Presente si estado=RECHAZADO|SUSPENDIDO
    private LocalDateTime fechaSolicitud;

    /** Datos del usuario asociado al comerciante */
    private UsuarioResumenDTO usuario;

    /** Documentos adjuntos para verificación (URLs de S3) */
    private List<String> documentosUrl;
}
