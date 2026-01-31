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
}
