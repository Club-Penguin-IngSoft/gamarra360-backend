package pe.com.gamarra360.backend.usuario.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.dto.ActualizarPerfilRequest;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;
import pe.com.gamarra360.backend.usuario.repository.UsuarioRepository;
import pe.com.gamarra360.backend.usuario.service.UsuarioService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsuarioServiceImpl extends AbstractCrudService<Usuario, Integer> implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    public UsuarioServiceImpl(UsuarioRepository repository,
                              ClienteRepository clienteRepository) {
        super(repository, "Usuario");
        this.usuarioRepository = repository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Usuario entidad, Integer id) {
        entidad.setUsuarioId(id);
    }

    @Override
    @Transactional
    public void actualizarPerfil(Integer id, ActualizarPerfilRequest request) {

        // Actualiza tabla usuarios
        if (request.nombres() != null || request.telefono() != null) {
            usuarioRepository.actualizarPerfil(
                    id,
                    request.nombres(),
                    request.primerApellido(),
                    request.segundoApellido(),
                    request.telefono()
            );
        }

        // Actualiza tabla clientes — nombre, apellido Y dirección
        clienteRepository.findById(id).ifPresent(cliente -> {
            if (request.nombres() != null) {
                cliente.setNombre(request.nombres());
            }
            if (request.primerApellido() != null) {
                cliente.setApellido(request.primerApellido());
            }
            if (request.direccionEntrega() != null) {
                cliente.setDireccionEntrega(request.direccionEntrega());
            }
            clienteRepository.save(cliente);
        });
    }
}
