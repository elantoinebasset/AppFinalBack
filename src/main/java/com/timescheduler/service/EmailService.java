package com.timescheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Envoie les emails via l'API HTTP de Brevo (port 443). On n'utilise pas le
 * SMTP car Render bloque les connexions SMTP sortantes. L'envoi est asynchrone
 * (fire-and-forget) pour ne jamais bloquer la requete HTTP.
 */
@ApplicationScoped
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @ConfigProperty(name = "app.backend.url")
    String backendUrl;

    @ConfigProperty(name = "app.mail.from")
    String fromEmail;

    @ConfigProperty(name = "app.brevo.api-key")
    Optional<String> brevoApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        String apiKey = brevoApiKey.map(String::trim).orElse("");
        if (apiKey.isBlank()) {
            Log.warn("BREVO_API_KEY non configure : email de verification non envoye.");
            return;
        }

        String link = backendUrl + "/api/auth/verify?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);

        String safeName = (firstName == null || firstName.isBlank()) ? "" : " " + firstName;

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; color: #1d232f;">
                  <h2>Bienvenue%s</h2>
                  <p>Merci de vous être inscrit sur <strong>Time Scheduler</strong>.</p>
                  <p>Pour activer votre compte, cliquez sur le bouton ci-dessous :</p>
                  <p style="text-align:center; margin: 28px 0;">
                    <a href="%s" style="background:#c45b2d; color:#ffffff; text-decoration:none; padding:12px 24px; border-radius:999px; font-weight:600; display:inline-block;">Vérifier mon adresse email</a>
                  </p>
                  <p style="font-size:13px; color:#5d6472;">Ou copiez ce lien dans votre navigateur :<br>%s</p>
                  <p style="font-size:13px; color:#5d6472;">Ce lien expire dans 24 heures.</p>
                </div>
                """.formatted(safeName, link, link);

        String body = buildPayload(toEmail, html);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_API_URL))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        // Envoi non-bloquant : la reponse HTTP de l'inscription n'attend pas l'envoi de l'email
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        Log.infof("Email de verification envoye a %s", toEmail);
                    } else {
                        Log.warnf("Echec envoi email a %s : HTTP %d - %s",
                                toEmail, response.statusCode(), response.body());
                    }
                })
                .exceptionally(error -> {
                    Log.warnf("Echec envoi email a %s : %s", toEmail, error.getMessage());
                    return null;
                });
    }

    private String buildPayload(String toEmail, String html) {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode sender = root.putObject("sender");
        sender.put("email", fromEmail);
        sender.put("name", "Time Scheduler");

        ArrayNode to = root.putArray("to");
        to.addObject().put("email", toEmail);

        root.put("subject", "Vérifiez votre adresse email — Time Scheduler");
        root.put("htmlContent", html);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de construire l'email", e);
        }
    }
}
