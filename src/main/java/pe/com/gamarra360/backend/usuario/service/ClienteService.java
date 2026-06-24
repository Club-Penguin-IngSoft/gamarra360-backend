package pe.com.gamarra360.backend.usuario.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.usuario.dto.ActualizarDatosPersonalesDto;
import pe.com.gamarra360.backend.usuario.dto.ActualizarDireccionClienteDto;
import pe.com.gamarra360.backend.usuario.dto.ActualizarNotificacionesDto;
import pe.com.gamarra360.backend.usuario.dto.PerfilClienteDto;
import pe.com.gamarra360.backend.usuario.entity.Cliente;

public interface ClienteService extends CrudService<Cliente, Integer> {

    PerfilClienteDto obtenerPerfil(Integer usuarioId);

    void actualizarDatosPersonales(Integer usuarioId, ActualizarDatosPersonalesDto dto);

    void actualizarDireccion(Integer usuarioId, ActualizarDireccionClienteDto dto);

    PerfilClienteDto actualizarNotificaciones(Integer usuarioId, ActualizarNotificacionesDto dto);
}
