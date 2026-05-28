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
import pe.com.gamarra360.backend.usuario.dto.GoogleLoginRequest;
import pe.com.gamarra360.backend.usuario.dto.GoogleAuthResponse;
import pe.com.gamarra360.backend.usuario.dto.GoogleRegistroRequest;
import pe.com.gamarra360.backend.usuario.entity.Admin;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.google.client-id}")
    private String googleClientId;

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
        usuario.setActivo(request.getRol() != RolEnum.VENDEDOR);
        usuario.setProveedorAuth(ProveedorAuth.LOCAL);
        Usuario guardado = usuarioRepository.save(usuario);
        
        String token = null;
        if (Boolean.TRUE.equals(guardado.getActivo())) {
            token = jwtService.generarToken(new UsuarioPrincipal(guardado));
        }
        return new AuthResponse(token, guardado.getUsuarioId(), guardado.getEmail(), guardado.getRol() == RolEnum.VENDEDOR ? "COMERCIANTE" : guardado.getRol().name());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login de usuario");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getContrasenha()));
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DatosInvalidosException("Credenciales invalidas."));
        String token = jwtService.generarToken(new UsuarioPrincipal(usuario));
        return new AuthResponse(token, usuario.getUsuarioId(), usuario.getEmail(), usuario.getRol() == RolEnum.VENDEDOR ? "COMERCIANTE" : usuario.getRol().name());
    }

    public GoogleAuthResponse autenticarConGoogle(GoogleLoginRequest request) {
        log.info("Autenticando con Google");
        GoogleIdToken.Payload payload = verificarGoogleToken(request.getIdToken());
        String email = payload.getEmail();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        GoogleAuthResponse response = new GoogleAuthResponse();

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            log.info("Usuario ya registrado con email: {}, procediendo a login directo con Google", email);
            
            if (usuario.getProveedorAuth() != ProveedorAuth.GOOGLE) {
                usuario.setProveedorAuth(ProveedorAuth.GOOGLE);
                usuarioRepository.save(usuario);
            }

            String token = jwtService.generarToken(new UsuarioPrincipal(usuario));
            response.setRegistrado(true);
            response.setToken(token);
            response.setUsuarioId(usuario.getUsuarioId());
            response.setEmail(usuario.getEmail());
            response.setRol(usuario.getRol() == RolEnum.VENDEDOR ? "COMERCIANTE" : usuario.getRol().name());
        } else {
            log.info("Usuario no registrado con email: {}, retornando datos basicos para registro", email);
            response.setRegistrado(false);
            response.setGoogleEmail(email);
            response.setNombres((String) payload.get("given_name"));
            response.setPrimerApellido((String) payload.get("family_name"));
        }

        return response;
    }

    @Transactional
    public AuthResponse registrarConGoogle(GoogleRegistroRequest request) {
        log.info("Registrando nuevo usuario mediante Google");
        GoogleIdToken.Payload payload = verificarGoogleToken(request.getIdToken());
        String email = payload.getEmail();

        if (usuarioRepository.existsByEmail(email)) {
            throw new DatosInvalidosException("El correo ya esta registrado.");
        }

        RegistroUsuarioRequest registroRequest = new RegistroUsuarioRequest();
        registroRequest.setRol(request.getRol());
        registroRequest.setNombres(request.getNombres());
        registroRequest.setPrimerApellido(request.getPrimerApellido());

        if (request.getRol() == RolEnum.VENDEDOR) {
            registroRequest.setRuc(request.getRuc());
            registroRequest.setRazonSocial(request.getRazonSocial());
        }

        Usuario usuario = crearUsuarioPorRol(registroRequest);
        usuario.setNombres(request.getNombres());
        usuario.setPrimerApellido(request.getPrimerApellido());
        usuario.setSegundoApellido(request.getSegundoApellido());
        usuario.setEmail(email);
        usuario.setContrasenha(passwordEncoder.encode(request.getContrasenha()));
        usuario.setDni(request.getNumeroDocumento());
        usuario.setTelefono(request.getCelular());
        usuario.setRol(request.getRol());
        usuario.setActivo(request.getRol() != RolEnum.VENDEDOR);
        usuario.setProveedorAuth(ProveedorAuth.GOOGLE);

        if (usuario instanceof Comerciante) {
            ((Comerciante) usuario).setIdTienda(request.getIdTienda());
        }

        Usuario guardado = usuarioRepository.save(usuario);
        
        String token = null;
        if (Boolean.TRUE.equals(guardado.getActivo())) {
            token = jwtService.generarToken(new UsuarioPrincipal(guardado));
        }
        return new AuthResponse(token, guardado.getUsuarioId(), guardado.getEmail(), guardado.getRol() == RolEnum.VENDEDOR ? "COMERCIANTE" : guardado.getRol().name());
    }

    private GoogleIdToken.Payload verificarGoogleToken(String tokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    new GsonFactory()
                )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            GoogleIdToken idToken = verifier.verify(tokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                log.error("Token de Google invalido");
                throw new DatosInvalidosException("Token de Google invalido.");
            }
        } catch (Exception e) {
            log.error("Error al verificar token de Google", e);
            throw new DatosInvalidosException("Error al verificar autenticacion con Google.");
        }
    }

    private Usuario crearUsuarioPorRol(RegistroUsuarioRequest request) {
        if (request.getRol() == RolEnum.CLIENTE) {
            Cliente cliente = new Cliente();
            cliente.setNombre(request.getNombres());
            cliente.setApellido(request.getPrimerApellido());
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
