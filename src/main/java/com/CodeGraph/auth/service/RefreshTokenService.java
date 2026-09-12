package com.CodeGraph.auth.service;

import com.CodeGraph.auth.mapper.RefreshTokenMapper;
import com.CodeGraph.auth.model.RefreshToken;
import com.CodeGraph.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String PURPOSE = "REFRESH";

    private final SecretKey secretKey;
    private final long expirationDays;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;

    public RefreshTokenService(
            @Value("${koode.jwt.refresh-secret}") String secret,
            @Value("${koode.jwt.refresh-expiration-days}") long expirationDays,
            RefreshTokenMapper refreshTokenMapper,
            PasswordEncoder passwordEncoder) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8)
                );

        this.expirationDays = expirationDays;
        this.refreshTokenMapper = refreshTokenMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateToken(
            User user,
            UUID sessionId) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("purpose", PURPOSE)
                .claim("sessionId", sessionId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        expirationDays,
                                        ChronoUnit.DAYS
                                )
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    public void save(
            User user,
            String refreshToken,
            String clientIp) {

        Claims claims =
                validateToken(refreshToken);

        RefreshToken entity =
                new RefreshToken();

        entity.setUserId(
                user.getId()
        );

        entity.setSessionId(
                UUID.fromString(
                        claims.get(
                                "sessionId",
                                String.class
                        )
                )
        );

        entity.setTokenId(
                claims.getId()
        );

        entity.setTokenHash(
                passwordEncoder.encode(
                        refreshToken
                )
        );

        entity.setCreatedAt(
                LocalDateTime.now()
        );

        entity.setCreatedIp(
                clientIp
        );

        entity.setExpiresAt(
                claims.getExpiration()
                        .toInstant()
                        .atZone(
                                ZoneId.systemDefault()
                        )
                        .toLocalDateTime()
        );

        refreshTokenMapper.insert(entity);
    }

    public Claims validateToken(
            String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public RefreshToken validateAndGetEntity(
            String refreshToken) {

        Claims claims =
                validateToken(refreshToken);

        /*
         * Check token purpose
         */
        if (!PURPOSE.equals(
                claims.get(
                        "purpose",
                        String.class
                ))) {

            throw new IllegalArgumentException(
                    "Invalid refresh token purpose"
            );
        }

        /*
         * Get session ID
         */
        String sessionIdValue =
                claims.get(
                        "sessionId",
                        String.class
                );

        if (sessionIdValue == null) {

            throw new IllegalArgumentException(
                    "Invalid refresh token session"
            );
        }

        UUID sessionId;

        try {

            sessionId =
                    UUID.fromString(
                            sessionIdValue
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid refresh token session"
            );
        }

        /*
         * Get JWT ID
         */
        String tokenId =
                claims.getId();

        if (tokenId == null) {

            throw new IllegalArgumentException(
                    "Invalid refresh token"
            );
        }

        /*
         * Find token in database
         */
        RefreshToken storedToken =
                refreshTokenMapper.findByTokenId(
                        tokenId
                );

        if (storedToken == null) {

            throw new IllegalArgumentException(
                    "Refresh token not found"
            );
        }

        /*
         * Make sure the session in the JWT
         * matches the database session.
         */
        if (!sessionId.equals(
                storedToken.getSessionId())) {

            throw new IllegalArgumentException(
                    "Invalid refresh token session"
            );
        }

        /*
         * Check if refresh token is revoked
         */
        if (storedToken.getRevokedAt() != null) {

            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        /*
         * Compare supplied token
         * against stored hash.
         */
        if (!passwordEncoder.matches(
                refreshToken,
                storedToken.getTokenHash())) {

            throw new IllegalArgumentException(
                    "Invalid refresh token"
            );
        }

        return storedToken;
    }

    @Transactional
    public void revokeBySessionId(UUID sessionId) {
        refreshTokenMapper.revokeBySessionId(sessionId);
    }

}