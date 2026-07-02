package com.timescheduler.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Verifies a Google ID token (the "credential" returned by Google Identity
 * Services on the frontend) by calling Google's tokeninfo endpoint, then checks
 * that the token was issued for our OAuth client and that the email is
 * verified.
 */
@ApplicationScoped
public class GoogleTokenVerifier {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @ConfigProperty(name = "app.auth.google.client-id")
    String clientId;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleUserInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Missing Google credential");
        }

        JsonNode payload = fetchTokenInfo(idToken);

        String aud = payload.path("aud").asText("");
        if (!clientId.equals(aud)) {
            throw new IllegalArgumentException("Google token was not issued for this application");
        }

        boolean emailVerified = payload.path("email_verified").asBoolean(false)
                || "true".equals(payload.path("email_verified").asText());
        String email = payload.path("email").asText("");
        if (email.isBlank() || !emailVerified) {
            throw new IllegalArgumentException("Google account email is not verified");
        }

        return new GoogleUserInfo(
                payload.path("sub").asText(""),
                email,
                payload.path("given_name").asText(""),
                payload.path("family_name").asText(""));
    }

    private JsonNode fetchTokenInfo(String idToken) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_INFO_URL + URLEncoder.encode(idToken, StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("Invalid Google credential");
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Could not verify Google credential");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not verify Google credential");
        }
    }

    public record GoogleUserInfo(String sub, String email, String firstName, String lastName) {

    }
}
