package com.timescheduler.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "app.auth.jwt.secret")
    String secret;

    @ConfigProperty(name = "app.auth.jwt.issuer")
    String issuer;

    @ConfigProperty(name = "app.auth.jwt.expiration-minutes")
    Long expirationMinutes;

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationMinutes * 60);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateAndExtractUsername(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }
}
