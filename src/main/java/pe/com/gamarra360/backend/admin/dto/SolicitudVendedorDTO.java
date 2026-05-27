package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Vista resumida de solicitud de vendedor para la cola de pendientes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudVendedorDTO {
    private Integer comercianteId;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String emailContacto;
    private String estado;           // PENDIENTE_APROBACION | APROBADO | RECHAZADO | SUSPENDIDO
    private LocalDateTime fechaSolicitud;
}
