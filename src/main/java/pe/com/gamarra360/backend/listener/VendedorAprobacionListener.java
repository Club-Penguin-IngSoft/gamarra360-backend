package pe.com.gamarra360.backend.listener;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.com.gamarra360.backend.admin.service.VendedorAprobadoEvent;
import pe.com.gamarra360.backend.admin.service.VendedorRechazadoEvent;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;

/**
 * Envía la notificación por correo cuando el administrador aprueba o rechaza
 * una solicitud de comerciante.
 *
 * Se ejecuta con {@code TransactionPhase.AFTER_COMMIT}: solo se envía el
 * correo si el cambio de estado (verificado/aprobado) ya quedó persistido con
 * éxito, igual que el patrón documentado en AdminUserService para
 * desactivar/reactivar cuentas.
 *
 * Cualquier error al enviar el correo se registra en el log pero NUNCA se
 * propaga — la aprobación/rechazo en la base de datos ya ocurrió antes de este
 * punto (la transacción ya hizo commit), así que un fallo de SMTP no debe
 * hacer parecer que la operación del administrador falló.
 *
 * Reutiliza el mismo mecanismo de envío (JavaMailSender + MimeMessageHelper)
 * que ya usa RecuperacionService para el código de recuperación de contraseña.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VendedorAprobacionListener {

    private final JavaMailSender mailSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAprobado(VendedorAprobadoEvent event) {
        Comerciante c = event.getComerciante();
        try {
            enviarCorreo(
                    c.getEmail(),
                    "Gamarra360 — ¡Tu cuenta de comerciante fue aprobada!",
                    construirHtmlAprobado(c)
            );
            log.info("Correo de aprobación enviado a: {}", c.getEmail());
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de aprobación a {}: {}", c.getEmail(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRechazado(VendedorRechazadoEvent event) {
        Comerciante c = event.getComerciante();
        try {
            enviarCorreo(
                    c.getEmail(),
                    "Gamarra360 — Actualización sobre tu solicitud de comerciante",
                    construirHtmlRechazado(c, event.getRazon())
            );
            log.info("Correo de rechazo enviado a: {}", c.getEmail());
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de rechazo a {}: {}", c.getEmail(), e.getMessage());
        }
    }

    // ── Envío de correo (mismo mecanismo que RecuperacionService) ──────────

    private void enviarCorreo(String destinatario, String asunto, String html) throws MessagingException {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se pudo enviar correo: el comerciante no tiene email registrado.");
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String saludo(Comerciante c) {
        return c.getNombres() != null && !c.getNombres().isBlank() ? " " + c.getNombres() : "";
    }

    private String construirHtmlAprobado(Comerciante c) {
        String tienda = c.getNombreTienda() != null ? c.getNombreTienda() : "tu tienda";
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Cuenta aprobada - Gamarra360</title></head>
        <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,Helvetica,sans-serif;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5f5f5;padding:40px 15px;">
                <tr><td align="center">
                    <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                        style="background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 8px 25px rgba(0,0,0,.08);">
                        <tr><td align="center" style="padding:45px 30px 30px;background:linear-gradient(to bottom,#ffffff,#fafafa);">
                            <div style="width:70px;height:4px;background:#198754;border-radius:10px;margin:0 auto 25px;"></div>
                            <h1 style="margin:0;font-size:28px;color:#222;font-weight:bold;">¡Cuenta de comerciante aprobada!</h1>
                        </td></tr>
                        <tr><td style="padding:45px;">
                            <p style="margin:0 0 20px;font-size:17px;line-height:28px;color:#555;text-align:center;">
                                Hola%s, tu solicitud como comerciante para <strong style="color:#D63384;">%s</strong>
                                en <strong style="color:#D63384;">GAMARRA360</strong> ha sido aprobada.
                            </p>
                            <p style="margin:0;font-size:16px;line-height:26px;color:#555;text-align:center;">
                                Ya puedes iniciar sesión con tu cuenta y comenzar a publicar tus productos.
                            </p>
                        </td></tr>
                        <tr><td align="center" style="background:#FAFAFA;border-top:1px solid #EEEEEE;padding:35px 20px;">
                            <p style="margin:0;font-size:13px;color:#999;">© 2026 <strong style="color:#D63384;">GAMARRA360</strong></p>
                        </td></tr>
                    </table>
                </td></tr>
            </table>
        </body>
        </html>
        """.formatted(saludo(c), tienda);
    }

    private String construirHtmlRechazado(Comerciante c, String razon) {
        String motivo = (razon != null && !razon.isBlank()) ? razon : "No se especificó un motivo.";
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Solicitud rechazada - Gamarra360</title></head>
        <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,Helvetica,sans-serif;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5f5f5;padding:40px 15px;">
                <tr><td align="center">
                    <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                        style="background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 8px 25px rgba(0,0,0,.08);">
                        <tr><td align="center" style="padding:45px 30px 30px;background:linear-gradient(to bottom,#ffffff,#fafafa);">
                            <div style="width:70px;height:4px;background:#D63384;border-radius:10px;margin:0 auto 25px;"></div>
                            <h1 style="margin:0;font-size:28px;color:#222;font-weight:bold;">Tu solicitud no fue aprobada</h1>
                        </td></tr>
                        <tr><td style="padding:45px;">
                            <p style="margin:0 0 20px;font-size:17px;line-height:28px;color:#555;text-align:center;">
                                Hola%s, tu solicitud como comerciante en <strong style="color:#D63384;">GAMARRA360</strong>
                                no pudo ser aprobada por el siguiente motivo:
                            </p>
                            <table role="presentation" align="center" cellspacing="0" cellpadding="0" style="margin:0 auto;">
                                <tr><td style="background:#FEE2E2;color:#B91C1C;font-size:15px;padding:16px 24px;border-radius:12px;">
                                    %s
                                </td></tr>
                            </table>
                            <p style="margin:30px 0 0;font-size:15px;line-height:26px;color:#666;text-align:center;">
                                Si crees que esto es un error, puedes volver a postular corrigiendo la información indicada.
                            </p>
                        </td></tr>
                        <tr><td align="center" style="background:#FAFAFA;border-top:1px solid #EEEEEE;padding:35px 20px;">
                            <p style="margin:0;font-size:13px;color:#999;">© 2026 <strong style="color:#D63384;">GAMARRA360</strong></p>
                        </td></tr>
                    </table>
                </td></tr>
            </table>
        </body>
        </html>
        """.formatted(saludo(c), motivo);
    }
}
