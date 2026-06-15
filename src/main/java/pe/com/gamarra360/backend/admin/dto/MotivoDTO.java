package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Motivo de una acción administrativa (desactivación, rechazo, suspensión). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotivoDTO {
    private String razon;
}
