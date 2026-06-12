package pe.com.gamarra360.backend.usuario.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import pe.com.gamarra360.backend.enums.ProveedorAuth;
import pe.com.gamarra360.backend.enums.RolEnum;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
/*import pe.com.gamarra360.backend.exception.UsuarioNoRegistradoGoogleException;*/
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Optional;

import pe.com.gamarra360.backend.usuario.dto.GoogleLoginRequest;

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

    public AuthResponse googleLogin(GoogleLoginRequest request) {
        String email = obtenerEmailDesdeGoogle(request.getAccessToken());
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            log.info("USUARIO NO EXISTE EN BD - SE REQUIERE REGISTRO: {}", email);
            return new AuthResponse(
                    null,
                    null,
                    email,
                    "CLIENTE",
                    true
            );
        }
        Usuario usuario = optUsuario.get();
        //Verificar estado si es VENDEDOR
        if (RolEnum.VENDEDOR.equals(usuario.getRol())) {
            Comerciante comerciante = (Comerciante) usuario;

            // verificado = 0 → pendiente de revisión
            if (!Boolean.TRUE.equals(comerciante.getVerificado())) {
                log.info("COMERCIANTE PENDIENTE DE APROBACIÓN: {}", email);
                return new AuthResponse(null, null, email, "VENDEDOR", false, "PENDIENTE");
            }

            // verificado = 1, aprobado = 0 → rechazado
            if (!Boolean.TRUE.equals(comerciante.getAprobado())) {
                log.info("COMERCIANTE RECHAZADO: {}", email);
                return new AuthResponse(null, null, email, "VENDEDOR", false, "RECHAZADO");
            }
        }
        String token = jwtService.generarToken(new UsuarioPrincipal(usuario));
        AuthResponse response = new AuthResponse(
                token,
                usuario.getUsuarioId(),
                usuario.getEmail(),
                usuario.getNombres(),
                usuario.getRol().name(),
                false
        );
        if (usuario instanceof Cliente cliente) {
            response.setDireccionEntrega(cliente.getDireccionEntrega());
        }
        return response;
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
        return new AuthResponse(token, guardado.getUsuarioId(), guardado.getEmail(),guardado.getNombres() ,guardado.getRol().name(),false);
    }

    public AuthResponse registrarGoogle(RegistroUsuarioRequest request) {
        Cliente usuario = new Cliente(); //
        usuario.setNombres(request.getNombres());
        usuario.setPrimerApellido(request.getPrimerApellido());
        usuario.setSegundoApellido(request.getSegundoApellido());
        usuario.setEmail(request.getEmail());
        usuario.setContrasenha(passwordEncoder.encode(request.getContrasenha()));
        usuario.setDni(request.getDni());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(request.getRol() != null ? request.getRol() : RolEnum.CLIENTE);
        usuario.setActivo(true);
        usuario.setTipoDocumento(request.getTipoDocumento());
        usuario.setProveedorAuth(ProveedorAuth.GOOGLE);
        //Cliente
        usuario.setNombre(request.getNombres());
        usuario.setApellido(request.getPrimerApellido());
        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(new UsuarioPrincipal(guardado));
        return new AuthResponse(
                token,
                guardado.getUsuarioId(),
                guardado.getEmail(),
                guardado.getNombres(),
                guardado.getRol().name(),
                false
        );
    }

    @Transactional
    public AuthResponse registrarComercianteGoogle(RegistroUsuarioRequest request) {
        log.info("Registro comerciante con Google: {}", request.getEmail());
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DatosInvalidosException("El correo ya está registrado.");
        }
        // 1. Usuario
        Comerciante usuario = new Comerciante();
        usuario.setNombres(request.getNombres());
        usuario.setPrimerApellido(request.getPrimerApellido());
        usuario.setSegundoApellido(request.getSegundoApellido());
        usuario.setEmail(request.getEmail());
        usuario.setContrasenha(passwordEncoder.encode(request.getContrasenha())); // placeholder
        usuario.setDni(request.getDni());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(RolEnum.VENDEDOR);
        usuario.setActivo(true);
        usuario.setProveedorAuth(ProveedorAuth.GOOGLE);
        // 2. Comerciante
        //Comerciante comerciante = new Comerciante();
        usuario.setRuc(request.getRuc());
        usuario.setRazonSocial(request.getRazonSocial());
        usuario.setVerificado(false);//los que faltan revisar
        usuario.setTipoDocumento(request.getTipoDocumento());
        usuario.setNombreTienda(request.getNombreTienda());
        usuario.setNombreComerciante(request.getNombres());
        usuario.setApellidoComerciante(request.getPrimerApellido());
        usuario.setAprobado(false);//aprueba o rechaza
        Usuario savedUser = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(new UsuarioPrincipal(savedUser));
        return new AuthResponse(
                token,
                savedUser.getUsuarioId(),
                savedUser.getEmail(),
                savedUser.getNombres(),
                savedUser.getRol().name(),
                false
        );
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login de usuario");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getContrasenha()));
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DatosInvalidosException("Credenciales invalidas."));
        String token = jwtService.generarToken(new UsuarioPrincipal(usuario));
        return new AuthResponse(token, usuario.getUsuarioId(), usuario.getEmail(), usuario.getNombres(), usuario.getRol().name(), false);
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
    private String obtenerEmailDesdeGoogle(String accessToken) {
        try {
            log.info("ACCESS TOKEN RECIBIDO: {}", accessToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            log.info("RESPUESTA GOOGLE RAW: {}", response);
            Map body = response.getBody();
            log.info("BODY GOOGLE: {}", body);
            if (body == null || body.get("email") == null) {
                throw new DatosInvalidosException("Token de Google inválido");
            }
            String email = body.get("email").toString();
            log.info("EMAIL OBTENIDO DE GOOGLE: {}", email);
            return email;
        } catch (Exception e) {
            log.error("ERROR VALIDANDO GOOGLE TOKEN", e);
            throw new DatosInvalidosException("Error validando token de Google: " + e.getMessage());
        }
    }
}