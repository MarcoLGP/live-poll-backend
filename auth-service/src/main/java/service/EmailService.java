package service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.MailerName;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    Mailer mailer;

    @Inject
    @MailerName("secondary")
    Mailer secondaryMailer;

    @Inject
    @Location("confirm-email.html")
    Template confirmEmailTemplate;

    @Inject
    @Location("reset-password.html")
    Template resetPasswordTemplate;

    @Inject
    @Location("password-changed.html")
    Template passwordChangedTemplate;

    @ConfigProperty(name = "frontend.url")
    String frontendUrl;

    public void sendConfirmationEmail(String to, String username, String token) {
        String confirmationUrl = frontendUrl + "/confirm-account?token=" + token;
        String html = confirmEmailTemplate
                .data("username", username)
                .data("confirmationUrl", confirmationUrl)
                .render();
        send(Mail.withHtml(to, "Confirme seu e-mail — LivePoll", html));
    }

    public void sendPasswordResetEmail(String to, String username, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String html = resetPasswordTemplate
                .data("username", username)
                .data("resetUrl", resetUrl)
                .render();
        send(Mail.withHtml(to, "Recuperar senha — LivePoll", html));
    }

    public void sendPasswordChangedEmail(String to, String username, String email) {
        String changedAt = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm")
                .withZone(java.time.ZoneId.of("America/Sao_Paulo"))
                .format(java.time.Instant.now());

        String html = passwordChangedTemplate
                .data("username", username)
                .data("email", email)
                .data("changedAt", changedAt)
                .data("loginUrl", frontendUrl + "/login")
                .render();
        send(Mail.withHtml(to, "Senha alterada — LivePoll", html));
    }

    /**
     * Tenta enviar. Se falhar (quota, auth, etc.),
     * usa Secondary como fallback.
     */
    private void send(Mail mail) {
        try {
            mailer.send(mail);
        } catch (Exception e) {
            LOG.warnf("SendGrid falhou (%s). Tentando fallback Secondary...", e.getMessage());
            try {
                secondaryMailer.send(mail);
            } catch (Exception ex) {
                LOG.errorf("Falha também no Secondary: %s", ex.getMessage());
                throw new RuntimeException("Falha ao enviar e-mail por todos os provedores.", ex);
            }
        }
    }
}