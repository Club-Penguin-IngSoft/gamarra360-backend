package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Resumen cuantitativo de actividad del usuario, para auditoría. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenActividadDTO {
    private long totalPedidos;
    private long totalCotizaciones;
    private long totalSolicitudes;
    private LocalDateTime ultimaConexion;
}
