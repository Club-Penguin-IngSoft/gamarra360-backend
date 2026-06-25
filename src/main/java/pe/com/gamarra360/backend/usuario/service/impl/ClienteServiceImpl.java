package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.logistica.entity.DistritoEnvio;
import pe.com.gamarra360.backend.logistica.repository.DistritoEnvioRepository;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.dto.ActualizarDatosPersonalesDto;
import pe.com.gamarra360.backend.usuario.dto.ActualizarDireccionClienteDto;
import pe.com.gamarra360.backend.usuario.dto.ActualizarNotificacionesDto;
import pe.com.gamarra360.backend.usuario.dto.PerfilClienteDto;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;
import pe.com.gamarra360.backend.usuario.service.ClienteService;

@Service
@Slf4j
public class ClienteServiceImpl extends AbstractCrudService<Cliente, Integer> implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final DistritoEnvioRepository distritoEnvioRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
                              DistritoEnvioRepository distritoEnvioRepository) {
        super(clienteRepository, "Cliente");
        this.clienteRepository = clienteRepository;
        this.distritoEnvioRepository = distritoEnvioRepository;
    }

    @Override
    protected Logger getLog() { return log; }

    @Override
    protected void asignarId(Cliente entidad, Integer id) { entidad.setUsuarioId(id); }

    // ── Perfil ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PerfilClienteDto obtenerPerfil(Integer usuarioId) {
        Cliente cliente = clienteRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + usuarioId));

        DistritoEnvio distrito = cliente.getIdDistrito() != null
                ? distritoEnvioRepository.findById(cliente.getIdDistrito()).orElse(null)
                : null;

        return new PerfilClienteDto(
                cliente.getEmail(),
                cliente.getNombres(),
                cliente.getPrimerApellido(),
                cliente.getSegundoApellido(),
                cliente.getTipoDocumento(),
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getDireccionEntrega(),
                cliente.getReferencia(),
                cliente.getIdDistrito(),
                distrito != null ? distrito.getNombre() : null,
                distrito != null ? distrito.getCiudad() : null,
                Boolean.TRUE.equals(cliente.getAlertasCorreo()),
                Boolean.TRUE.equals(cliente.getNotificacionesPush())
        );
    }

    @Override
    public void actualizarDatosPersonales(Integer usuarioId, ActualizarDatosPersonalesDto dto) {
        Cliente cliente = clienteRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + usuarioId));

        cliente.setNombres(dto.nombres());
        cliente.setPrimerApellido(dto.primerApellido());
        cliente.setSegundoApellido(dto.segundoApellido());
        cliente.setTelefono(dto.telefono());

        clienteRepository.save(cliente);
        log.info("Datos personales actualizados — usuarioId={}", usuarioId);
    }

    @Override
    public void actualizarDireccion(Integer usuarioId, ActualizarDireccionClienteDto dto) {
        Cliente cliente = clienteRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + usuarioId));

        cliente.setDireccionEntrega(dto.direccionEntrega());
        cliente.setReferencia(dto.referencia());

        if (dto.idDistrito() != null) {
            distritoEnvioRepository.findById(dto.idDistrito())
                    .orElseThrow(() -> new DatosInvalidosException(
                            "Distrito no válido: " + dto.idDistrito()));
            cliente.setIdDistrito(dto.idDistrito());
        } else {
            cliente.setIdDistrito(null);
        }

        clienteRepository.save(cliente);
        log.info("Dirección actualizada — usuarioId={}", usuarioId);
    }

    @Override
    @Transactional
    public PerfilClienteDto actualizarNotificaciones(Integer usuarioId, ActualizarNotificacionesDto dto) {
        Cliente cliente = clienteRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + usuarioId));

        cliente.setAlertasCorreo(dto.alertasCorreo());
        cliente.setNotificacionesPush(dto.notificacionesPush());
        clienteRepository.save(cliente);

        log.info("Notificaciones actualizadas — usuarioId={}, alertasCorreo={}, notificacionesPush={}",
                usuarioId, dto.alertasCorreo(), dto.notificacionesPush());

        return obtenerPerfil(usuarioId);
    }
}
