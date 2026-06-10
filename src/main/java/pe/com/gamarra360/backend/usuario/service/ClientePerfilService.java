package pe.com.gamarra360.backend.usuario.service;

import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import pe.com.gamarra360.backend.usuario.dto.perfil.*;

import java.util.List;

public interface ClientePerfilService {
    ClienteResumenCuentaResponse obtenerResumenCuenta(UsuarioPrincipal principal);
    ClientePerfilResponse obtenerPerfil(UsuarioPrincipal principal);
    ClientePerfilResponse actualizarPerfil(UsuarioPrincipal principal, ClientePerfilActualizarRequest request);
    ClientePerfilResponse actualizarDireccion(UsuarioPrincipal principal, ClienteDireccionRequest request);
    ClientePreferenciasNotificacionDto actualizarPreferencias(UsuarioPrincipal principal, ClientePreferenciasNotificacionRequest request);
    void cambiarPassword(UsuarioPrincipal principal, ClienteCambiarPasswordRequest request);
    void desactivarCuenta(UsuarioPrincipal principal);

    List<ClientePedidoResumenDto> listarPedidos(UsuarioPrincipal principal);
    List<ClientePedidoResumenDto> listarPedidosRecientes(UsuarioPrincipal principal, int limite);
    ClientePedidoDetalleDto obtenerPedido(UsuarioPrincipal principal, Long pedidoId);
    ClientePedidoDetalleDto cancelarPedido(UsuarioPrincipal principal, Long pedidoId);

    List<ClienteSolicitudResumenDto> listarPersonalizaciones(UsuarioPrincipal principal);
    ClienteSolicitudResumenDto obtenerPersonalizacion(UsuarioPrincipal principal, Long personalizacionId);
    List<ClienteSolicitudResumenDto> listarCotizaciones(UsuarioPrincipal principal);
    ClienteSolicitudResumenDto obtenerCotizacion(UsuarioPrincipal principal, Long cotizacionId);
}
