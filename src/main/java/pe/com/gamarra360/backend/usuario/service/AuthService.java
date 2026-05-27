package pe.com.gamarra360.backend.usuario.service;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.enums.ProveedorAuth;
import pe.com.gamarra360.backend.enums.RolEnum;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.security.JwtService;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import pe.com.gamarra360.backend.usuario.dto.AuthResponse;
import pe.com.gamarra360.backend.usuario.dto.LoginRequest;
import pe.com.gamarra360.backend.usuario.dto.RegistroUsuarioRequest;
import pe.com.gamarra360.backend.usuario.entity.Admin;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse registrar(RegistroUsuarioRequest request) {
        log.info("Registrando usuario con rol {}", request.getRol());
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DatosInvalidosException("El correo ya esta registrado.");
        }
        Usuario usuario = crearUsuarioPorRol(request);
        usuario.setNombres(request.getNombres());
        usuario.setPrimerApellido(request.getPrimerApellido());
        usuario.setSegundoApellido(request.getSegundoApellido());
        usuario.setEmail(request.getEmail());
        usuario.setContrasenha(passwordEncoder.encode(request.getContrasenha()));
        usuario.setDni(request.getDni());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(request.getRol());
        usuario.setActivo(true);
        usuario.setProveedorAuth(ProveedorAuth.LOCAL);
        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(new UsuarioPrincipal(guardado));
        return new AuthResponse(token, guardado.getUsuarioId(), guardado.getEmail(), guardado.getRol().name());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login de usuario");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getContrasenha()));
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DatosInvalidosException("Credenciales invalidas."));
        String token = jwtService.generarToken(new UsuarioPrincipal(usuario));
        return new AuthResponse(token, usuario.getUsuarioId(), usuario.getEmail(), usuario.getRol().name());
    }

    private Usuario crearUsuarioPorRol(RegistroUsuarioRequest request) {
        if (request.getRol() == RolEnum.CLIENTE) {
            Cliente cliente = new Cliente();
            cliente.setNombre(request.getNombre());
            cliente.setApellido(request.getApellido());
            return cliente;
        }
        if (request.getRol() == RolEnum.VENDEDOR) {
            Comerciante comerciante = new Comerciante();
            comerciante.setRuc(request.getRuc());
            comerciante.setRazonSocial(request.getRazonSocial());
            comerciante.setVerificado(false);
            return comerciante;
        }
        Admin admin = new Admin();
        return admin;
    }
}
