package pe.com.gamarra360.backend.usuario.dto.perfil;

import java.time.LocalDateTime;
import java.util.List;

public record ClienteSolicitudResumenDto(
        Long id,
        String numeroSolicitud,
        LocalDateTime fecha,
        String fechaTexto,
        String estado,
        String estadoTexto,
        String tipo,
        String descripcion,
        String imagenUrl,
        Double precioPropuesto,
        ClienteTiendaResumenDto tienda,
        List<ClienteProductoResumenDto> items
) {}
