package com.CodeGraph.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class RegistrationTokenService {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public RegistrationTokenService(
            @Value("${koode.jwt.registration-secret}")
            String secret,
            @Value("${koode.jwt.registration-expiration-minutes}")
            long expirationMinutes) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String email) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .claim("purpose", "REGISTER")
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        expirationMinutes,
                                        ChronoUnit.MINUTES
                                )
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    public String validateAndGetEmail(String token) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

        String purpose =
                claims.get("purpose", String.class);

        if (!"REGISTER".equals(purpose)) {
            throw new IllegalArgumentException(
                    "Invalid registration token"
            );
        }

        return claims.getSubject();
    }
}