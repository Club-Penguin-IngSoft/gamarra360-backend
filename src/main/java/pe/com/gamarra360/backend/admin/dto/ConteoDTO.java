package pe.com.gamarra360.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Conteo genérico para badges y métricas del panel. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConteoDTO {
    private long total;
}
