package com.CodeGraph.auth.service;

import com.CodeGraph.user.model.User;
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
import java.util.UUID;

@Service
public class AccessTokenService {

    private static final String PURPOSE = "ACCESS";

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public AccessTokenService(
            @Value("${koode.jwt.access-secret}")
            String secret,

            @Value("${koode.jwt.access-expiration-minutes}")
            long expirationMinutes) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        this.expirationMinutes =
                expirationMinutes;
    }

    // =========================================================
    // Generate Access Token
    // =========================================================

    public String generateToken(
            User user,
            UUID sessionId,
            UUID accessTokenId) {

        Instant now =
                Instant.now();

        return Jwts.builder()
                .subject(
                        user.getId().toString()
                )
                .claim(
                        "email",
                        user.getEmail()
                )
                .claim(
                        "purpose",
                        PURPOSE
                )
                .claim(
                        "sessionId",
                        sessionId.toString()
                )
                .id(
                        accessTokenId.toString()
                )
                .issuedAt(
                        Date.from(now)
                )
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

    // =========================================================
    // Validate Access Token
    // =========================================================

    public Claims validateToken(
            String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =========================================================
    // Get User ID
    // =========================================================

    public Long getUserId(
            String token) {

        Claims claims =
                validateToken(token);

        validatePurpose(claims);

        return Long.valueOf(
                claims.getSubject()
        );
    }

    // =========================================================
    // Get Email
    // =========================================================

    public String getEmail(
            String token) {

        Claims claims =
                validateToken(token);

        validatePurpose(claims);

        return claims.get(
                "email",
                String.class
        );
    }

    // =========================================================
    // Get Session ID
    // =========================================================

    public UUID getSessionId(
            String token) {

        Claims claims =
                validateToken(token);

        validatePurpose(claims);

        String sessionId =
                claims.get(
                        "sessionId",
                        String.class
                );

        if (sessionId == null) {

            throw new IllegalArgumentException(
                    "Invalid access token session"
            );
        }

        try {

            return UUID.fromString(sessionId);

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid access token session"
            );
        }
    }

    // =========================================================
    // Get Access Token ID
    // =========================================================

    public UUID getAccessTokenId(
            String token) {

        Claims claims =
                validateToken(token);

        validatePurpose(claims);

        String accessTokenId =
                claims.getId();

        if (accessTokenId == null) {

            throw new IllegalArgumentException(
                    "Invalid access token ID"
            );
        }

        try {

            return UUID.fromString(accessTokenId);

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid access token ID"
            );
        }
    }

    // =========================================================
    // Validate Purpose
    // =========================================================

    private void validatePurpose(
            Claims claims) {

        String purpose =
                claims.get(
                        "purpose",
                        String.class
                );

        if (!PURPOSE.equals(purpose)) {

            throw new IllegalArgumentException(
                    "Invalid access token"
            );
        }
    }
}