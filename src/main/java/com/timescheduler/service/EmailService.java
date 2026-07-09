package com.timescheduler.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "app.backend.url")
    String backendUrl;

    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        String link = backendUrl + "/api/auth/verify?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);

        String safeName = (firstName == null || firstName.isBlank()) ? "" : " " + firstName;

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; color: #1d232f;">
                  <h2>Bienvenue%s </h2>
                  <p>Merci de vous être inscrit sur <strong>Time Scheduler</strong>.</p>
                  <p>Pour activer votre compte, cliquez sur le bouton ci-dessous :</p>
                  <p style="text-align:center; margin: 28px 0;">
                    <a href="%s" style="background:#c45b2d; color:#ffffff; text-decoration:none; padding:12px 24px; border-radius:999px; font-weight:600; display:inline-block;">Vérifier mon adresse email</a>
                  </p>
                  <p style="font-size:13px; color:#5d6472;">Ou copiez ce lien dans votre navigateur :<br>%s</p>
                  <p style="font-size:13px; color:#5d6472;">Ce lien expire dans 24 heures.</p>
                </div>
                """.formatted(safeName, link, link);

        mailer.send(Mail.withHtml(toEmail, "Vérifiez votre adresse email — Time Scheduler", html));
    }
}
