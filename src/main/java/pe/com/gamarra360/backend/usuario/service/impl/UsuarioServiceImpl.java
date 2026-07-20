package pe.com.gamarra360.backend.usuario.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.dto.ActualizarPerfilRequest;
import pe.com.gamarra360.backend.usuario.dto.CambiarPasswordRequest;
import pe.com.gamarra360.backend.enums.ProveedorAuth;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    public UsuarioServiceImpl(UsuarioRepository repository,
                              ClienteRepository clienteRepository,
                              PasswordEncoder passwordEncoder) {
        super(repository, "Usuario");
        this.usuarioRepository = repository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
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

        // Actualiza dirección en tabla clientes (identidad ya actualizada en usuarios)
        if (request.direccionEntrega() != null) {
            clienteRepository.findById(id).ifPresent(cliente -> {
                cliente.setDireccionEntrega(request.direccionEntrega());
                clienteRepository.save(cliente);
            });
        }
    }

    @Override
    @Transactional
    public void cambiarPassword(Integer id, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new DatosInvalidosException("Usuario no encontrado."));
        if (ProveedorAuth.GOOGLE.equals(usuario.getProveedorAuth())) {
            throw new DatosInvalidosException("Las cuentas de Google administran su contraseña desde Google.");
        }
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getContrasenha())) {
            throw new DatosInvalidosException("La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(request.passwordNueva(), usuario.getContrasenha())) {
            throw new DatosInvalidosException("La nueva contraseña debe ser diferente de la actual.");
        }
        usuario.setContrasenha(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }
}
