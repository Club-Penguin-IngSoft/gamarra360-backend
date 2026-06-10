package pe.com.gamarra360.backend.usuario.dto.perfil;

import java.time.LocalDateTime;
import java.util.List;

public record ClientePedidoDetalleDto(
        Long id,
        String numeroPedido,
        LocalDateTime fecha,
        String fechaTexto,
        Double total,
        String estado,
        String estadoTexto,
        String tipoEntrega,
        String tipoEntregaTexto,
        String direccionEntrega,
        ClienteTiendaResumenDto tienda,
        List<ClienteProductoResumenDto> items,
        Boolean puedeCancelar
) {}
