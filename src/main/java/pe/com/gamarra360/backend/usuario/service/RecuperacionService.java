package pe.com.gamarra360.backend.usuario.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.enums.ProveedorAuth;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.usuario.entity.CodigoVerificacion;
import pe.com.gamarra360.backend.usuario.entity.Usuario;
import pe.com.gamarra360.backend.usuario.repository.CodigoVerificacionRepository;
import pe.com.gamarra360.backend.usuario.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecuperacionService {

    private final UsuarioRepository usuarioRepository;
    private final CodigoVerificacionRepository codigoRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // ── PASO 1: Solicitar código ─────────────────────────────────────────

    @Transactional
    public void solicitarCodigo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una cuenta con ese correo."));

        // Bloquear cuentas Google — no tienen contraseña propia
        if (ProveedorAuth.GOOGLE.equals(usuario.getProveedorAuth())) {
            throw new DatosInvalidosException(
                    "Este correo está registrado con Google. Inicia sesión con el botón de Google.");
        }

        // Invalidar códigos anteriores para este email
        codigoRepo.invalidarTodosPorEmail(email);

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);

        codigoRepo.save(new CodigoVerificacion(email, codigo, expiracion));

        enviarCorreo(email, codigo, usuario.getNombres());
        log.info("Código de recuperación enviado a: {}", email);
    }

    // ── PASO 2: Verificar código ─────────────────────────────────────────

    public void verificarCodigo(String email, String codigo) {
        CodigoVerificacion cv = codigoRepo
                .findTopByEmailOrderByExpiracionDesc(email)
                .orElseThrow(() -> new DatosInvalidosException("Código inválido o expirado."));

        if (!cv.estaVigente()) {
            throw new DatosInvalidosException("El código ha expirado. Solicita uno nuevo.");
        }
        if (!cv.getCodigo().equals(codigo)) {
            throw new DatosInvalidosException("El código ingresado es incorrecto.");
        }
        // No marcamos como usado aquí — lo hacemos al restablecer,
        // así el frontend puede reenviar en caso de error de red.
    }

    // ── PASO 3: Restablecer contraseña ───────────────────────────────────

    @Transactional
    public void restablecerPassword(String email, String codigo, String nuevaContrasenha) {
        // Validar código nuevamente (evita saltar el paso 2)
        CodigoVerificacion cv = codigoRepo
                .findTopByEmailOrderByExpiracionDesc(email)
                .orElseThrow(() -> new DatosInvalidosException("Código inválido o expirado."));

        if (!cv.estaVigente()) {
            throw new DatosInvalidosException("El código ha expirado. Solicita uno nuevo.");
        }
        if (!cv.getCodigo().equals(codigo)) {
            throw new DatosInvalidosException("El código ingresado es incorrecto.");
        }

        // Actualizar contraseña
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        usuario.setContrasenha(passwordEncoder.encode(nuevaContrasenha));
        usuarioRepository.save(usuario);

        // Invalidar el código ya usado
        cv.setUsado(true);
        codigoRepo.save(cv);

        log.info("Contraseña restablecida para: {}", email);
    }

    // ── Envío de correo ──────────────────────────────────────────────────

    private void enviarCorreo(String destinatario, String codigo, String nombre) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Gamarra360 — Código de verificación");
            helper.setText(construirHtml(nombre, codigo), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error enviando correo a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo. Intenta de nuevo.");
        }
    }

    private String construirHtml(String nombre, String codigo) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Recuperación de contraseña - Gamarra360</title>
        </head>
        <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,Helvetica,sans-serif;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5f5f5;padding:40px 15px;">
                <tr>
                    <td align="center">
                        <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                            style="background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 8px 25px rgba(0,0,0,.08);">

                            <!-- Header -->
                            <tr>
                                <td align="center" style="padding:45px 30px 30px;background:linear-gradient(to bottom,#ffffff,#fafafa);">
                                    <img src="https://i.ibb.co/6R2GD1n5/Captura-de-pantalla-2026-06-29-202747.png"
                                        alt="Gamarra360" width="220"
                                        style="display:block;border:0;outline:none;text-decoration:none;margin:0 auto 25px;max-width:220px;">
                                    <div style="width:70px;height:4px;background:#D63384;border-radius:10px;margin:0 auto 25px;"></div>
                                    <h1 style="margin:0;font-size:30px;color:#222;font-weight:bold;">
                                        Recuperación de contraseña
                                    </h1>
                                </td>
                            </tr>

                            <!-- Contenido -->
                            <tr>
                                <td style="padding:45px;">
                                    <p style="margin:0 0 10px;font-size:18px;color:#222;text-align:center;font-weight:bold;">
                                        Hola%s,
                                    </p>
                                    <p style="margin:0 0 30px;font-size:17px;line-height:30px;color:#555;text-align:center;">
                                        Hemos recibido una solicitud para recuperar la contraseña de tu cuenta de
                                        <strong style="color:#D63384;">GAMARRA360</strong>.
                                        <br><br>
                                        Este es tu código de verificación:
                                    </p>

                                    <!-- Código -->
                                    <table role="presentation" align="center" cellspacing="0" cellpadding="0">
                                        <tr>
                                            <td align="center"
                                                style="background:#D63384;color:#ffffff;font-size:42px;font-weight:bold;letter-spacing:8px;padding:18px 55px;border-radius:14px;">
                                                %s
                                            </td>
                                        </tr>
                                    </table>

                                    <p style="margin:35px 0 0;text-align:center;color:#666;font-size:15px;line-height:26px;">
                                        Este código es válido durante <strong>10 minutos</strong> y solo puede utilizarse una vez.
                                    </p>
                                </td>
                            </tr>

                            <!-- Separador -->
                            <tr>
                                <td style="padding:0 45px;">
                                    <hr style="border:none;border-top:1px solid #EEEEEE;">
                                </td>
                            </tr>

                            <!-- Seguridad -->
                            <tr>
                                <td style="padding:30px 45px;">
                                    <p style="margin:0;color:#777;font-size:15px;line-height:26px;text-align:center;">
                                        Si no solicitaste este código, puedes ignorar este correo.
                                        Nadie podrá acceder a tu cuenta sin esta verificación.
                                    </p>
                                </td>
                            </tr>

                            <!-- Footer -->
                            <tr>
                                <td align="center" style="background:#FAFAFA;border-top:1px solid #EEEEEE;padding:35px 20px;">
                                    <p style="margin:0;font-size:13px;color:#999;">
                                        © 2026 <strong style="color:#D63384;">GAMARRA360</strong>
                                    </p>
                                    <p style="margin:10px 0 0;font-size:15px;color:#444;">
                                        Todo lo que buscas, solo en
                                        <span style="color:#D63384;font-weight:bold;">Gamarra</span>
                                    </p>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(
                nombre != null && !nombre.isBlank() ? " " + nombre : "",
                codigo
        );
    }
}
