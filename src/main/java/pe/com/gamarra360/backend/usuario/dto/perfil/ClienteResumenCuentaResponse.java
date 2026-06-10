package pe.com.gamarra360.backend.usuario.dto.perfil;

import java.util.List;

public record ClienteResumenCuentaResponse(
        ClientePerfilResponse perfil,
        List<ClientePedidoResumenDto> pedidosRecientes,
        Long notificacionesNoLeidas
) {}
