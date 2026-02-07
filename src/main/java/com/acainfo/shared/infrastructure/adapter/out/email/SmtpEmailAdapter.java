package com.acainfo.shared.infrastructure.adapter.out.email;

import com.acainfo.shared.application.port.out.EmailSenderPort;
import com.acainfo.shared.infrastructure.config.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * SMTP email adapter implementing the EmailSenderPort.
 * Uses Spring's JavaMailSender for actual email delivery.
 * Supports mock mode for development/testing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    @Async
    public void sendVerificationEmail(String to, String userName, String verificationLink) {
        String subject = "Verifica tu cuenta de AcaInfo";
        String htmlContent = buildVerificationEmailHtml(userName, verificationLink);

        if (emailProperties.isMock()) {
            log.info("=== MOCK EMAIL ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Verification Link: {}", verificationLink);
            log.info("==================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildVerificationEmailHtml(String userName, String verificationLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verifica tu cuenta</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0;">AcaInfo</h1>
                    <p style="color: rgba(255,255,255,0.9); margin-top: 10px;">Plataforma de Formacion Academica</p>
                </div>

                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #333;">Hola %s,</h2>

                    <p>Gracias por registrarte en AcaInfo. Para completar tu registro y activar tu cuenta, por favor verifica tu direccion de correo electronico.</p>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            Verificar mi cuenta
                        </a>
                    </div>

                    <p style="color: #666; font-size: 14px;">Si el boton no funciona, copia y pega el siguiente enlace en tu navegador:</p>
                    <p style="background: #eee; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 12px;">%s</p>

                    <p style="color: #666; font-size: 14px; margin-top: 20px;">
                        <strong>Importante:</strong> Este enlace expirara en 24 horas.
                    </p>

                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">

                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Si no has creado una cuenta en AcaInfo, puedes ignorar este correo.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(userName, verificationLink, verificationLink);
    }

    @Override
    @Async
    public void sendAccountDeactivatedEmail(String to, String userName, String reason) {
        String subject = "Tu cuenta de AcaInfo ha sido desactivada";
        String htmlContent = buildDeactivationEmailHtml(userName, reason);

        if (emailProperties.isMock()) {
            log.info("=== MOCK EMAIL (DEACTIVATION) ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Reason: {}", reason);
            log.info("=================================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Deactivation email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send deactivation email to {}: {}", to, e.getMessage());
            // Don't throw - deactivation should proceed even if email fails
        }
    }

    @Override
    @Async
    public void sendAccountReactivatedEmail(String to, String userName) {
        String subject = "Tu cuenta de AcaInfo ha sido reactivada";
        String htmlContent = buildReactivationEmailHtml(userName);

        if (emailProperties.isMock()) {
            log.info("=== MOCK EMAIL (REACTIVATION) ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("=================================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Reactivation email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send reactivation email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String userName, String resetLink) {
        String subject = "Restablecer tu contraseña de AcaInfo";
        String htmlContent = buildPasswordResetEmailHtml(userName, resetLink);

        if (emailProperties.isMock()) {
            log.info("=== MOCK EMAIL (PASSWORD RESET) ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Reset Link: {}", resetLink);
            log.info("===================================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildPasswordResetEmailHtml(String userName, String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Restablecer contraseña</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #f59e0b 0%%, #ea580c 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0;">AcaInfo</h1>
                    <p style="color: rgba(255,255,255,0.9); margin-top: 10px;">Recuperacion de contraseña</p>
                </div>

                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #333;">Hola %s,</h2>

                    <p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en AcaInfo.</p>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: linear-gradient(135deg, #f59e0b 0%%, #ea580c 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            Restablecer mi contraseña
                        </a>
                    </div>

                    <p style="color: #666; font-size: 14px;">Si el boton no funciona, copia y pega el siguiente enlace en tu navegador:</p>
                    <p style="background: #eee; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 12px;">%s</p>

                    <p style="color: #666; font-size: 14px; margin-top: 20px;">
                        <strong>Importante:</strong> Este enlace expirara en 1 hora.
                    </p>

                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">

                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Si no has solicitado restablecer tu contraseña, puedes ignorar este correo. Tu cuenta permanecera segura.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(userName, resetLink, resetLink);
    }

    private String buildDeactivationEmailHtml(String userName, String reason) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Cuenta desactivada</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0;">AcaInfo</h1>
                    <p style="color: rgba(255,255,255,0.9); margin-top: 10px;">Aviso importante sobre tu cuenta</p>
                </div>

                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #333;">Hola %s,</h2>

                    <p>Te informamos que tu cuenta en AcaInfo ha sido <strong>desactivada temporalmente</strong>.</p>

                    <div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0;"><strong>Motivo:</strong> %s</p>
                    </div>

                    <p>Mientras tu cuenta este desactivada, no podras acceder a los materiales ni funcionalidades de la plataforma.</p>

                    <h3 style="color: #333;">Como reactivar tu cuenta</h3>
                    <p>Para reactivar tu cuenta, por favor contacta con el centro:</p>

                    <ul>
                        <li>Telefono: +34 953 123 456</li>
                        <li>Email: info@acainfo.com</li>
                    </ul>

                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">

                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Este es un mensaje automatico. Si tienes dudas, contacta con nosotros.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(userName, reason);
    }

    private String buildReactivationEmailHtml(String userName) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Cuenta reactivada</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0;">AcaInfo</h1>
                    <p style="color: rgba(255,255,255,0.9); margin-top: 10px;">Buenas noticias</p>
                </div>

                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #333;">Hola %s,</h2>

                    <p>Nos complace informarte que tu cuenta en AcaInfo ha sido <strong>reactivada</strong>.</p>

                    <div style="background: #d1fae5; border-left: 4px solid #10b981; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0;">Ya puedes acceder nuevamente a todos los materiales y funcionalidades de la plataforma.</p>
                    </div>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://acadeinfo.com/login" style="background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            Acceder a mi cuenta
                        </a>
                    </div>

                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">

                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Gracias por confiar en AcaInfo.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(userName);
    }
}
