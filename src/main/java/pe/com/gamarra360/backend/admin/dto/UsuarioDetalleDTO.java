package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Vista detallada de un usuario para el panel lateral del administrador.
 * Incluye historial de actividad para auditoría.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDetalleDTO {
    private Integer usuarioId;
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String email;
    private String dni;
    private String telefono;
    private String rol;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    /** Resumen de actividad: pedidos, cotizaciones, solicitudes */
    private ResumenActividadDTO actividad;
}
