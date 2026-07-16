package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DashboardResumenDTO {
    private Long totalUsuarios;
    private Double ingresosTotales; // 10% de comisión sobre ventas pagadas
    private Long comerciantesPendientes;
    private Long comerciantesAprobados;
    private Long comerciantesRechazados;
    private List<ActividadRecienteDTO> actividadReciente;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ActividadRecienteDTO {
        private String tipo; // "COMERCIANTE" | "CLIENTE"
        private String nombreCompleto;
        private String email;
    }
}
