package com.CodeGraph.auth.security;

import com.CodeGraph.auth.mapper.AuthSessionMapper;
import com.CodeGraph.auth.model.AuthSession;
import com.CodeGraph.auth.service.AccessTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String ACCESS_PURPOSE = "ACCESS";

    private final AccessTokenService accessTokenService;
    private final AuthSessionMapper authSessionMapper;

    public JwtAuthenticationFilter(
            AccessTokenService accessTokenService,
            AuthSessionMapper authSessionMapper) {

        this.accessTokenService =
                accessTokenService;

        this.authSessionMapper =
                authSessionMapper;
    }

    /*
     * These endpoints don't require an access token.
     *
     * /api/auth/** is already permitAll() in SecurityConfig,
     * but the filter should also avoid trying to authenticate
     * these requests.
     */
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path =
                request.getServletPath();

        return path.equals("/api/auth/refresh")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/register/send-otp")
                || path.equals("/api/auth/register/verify-otp")
                || path.equals("/api/auth/register/set-password");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        /*
         * Extract token from HttpOnly cookie first,
         * with a fallback to the Authorization header.
         */
        String token = null;
        Cookie cookie = WebUtils.getCookie(request, "access_token");

        if (cookie != null) {
            token = cookie.getValue();
        } else {
            String authorization =
                    request.getHeader("Authorization");

            if (authorization != null
                    && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
        }

        /*
         * No token found in cookies or headers.
         *
         * Don't authenticate here.
         * Spring Security will decide whether
         * the endpoint requires authentication.
         */
        if (token == null) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {

            /*
             * Validate JWT.
             *
             * This checks:
             * - Signature
             * - Expiration
             */
            Claims claims =
                    accessTokenService.validateToken(
                            token
                    );

            /*
             * Validate token purpose
             */
            String purpose =
                    claims.get(
                            "purpose",
                            String.class
                    );

            if (!ACCESS_PURPOSE.equals(purpose)) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * Get user ID
             */
            String subject =
                    claims.getSubject();

            if (subject == null) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            Long userId =
                    Long.valueOf(subject);

            /*
             * Get email
             */
            String email =
                    claims.get(
                            "email",
                            String.class
                    );

            /*
             * Get session ID
             */
            String sessionIdValue =
                    claims.get(
                            "sessionId",
                            String.class
                    );

            if (sessionIdValue == null) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * Convert session ID
             * from JWT string to UUID.
             */
            UUID sessionId =
                    UUID.fromString(
                            sessionIdValue
                    );

            /*
             * Find authentication session
             */
            AuthSession session =
                    authSessionMapper.findBySessionId(
                            sessionId
                    );

            /*
             * Session does not exist
             */
            if (session == null) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * Make sure this session belongs
             * to the same user contained in JWT.
             */
            if (!userId.equals(
                    session.getUserId())) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * Check if session has been revoked.
             *
             * Logout sets revoked_at.
             */
            if (session.getRevokedAt() != null) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * Check session expiration.
             *
             * Session lifetime is the same as
             * refresh-token lifetime.
             */
            if (session.getExpiresAt() == null
                    || !session.getExpiresAt()
                    .isAfter(
                            LocalDateTime.now()
                    )) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * Get access-token ID (JWT jti)
             */
            String accessTokenIdValue =
                    claims.getId();

            if (accessTokenIdValue == null) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            UUID accessTokenId =
                    UUID.fromString(
                            accessTokenIdValue
                    );

            /*
             * Make sure this is the currently
             * active access token for the session.
             *
             * When a refresh happens, the
             * accessTokenId in auth_sessions is
             * replaced with a new value.
             *
             * Therefore, older access tokens
             * will fail this check.
             */
            if (session.getAccessTokenId() == null
                    || !accessTokenId.equals(
                    session.getAccessTokenId())) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }
            /*
             * Create authenticated user.
             *
             * This contains:
             * - userId
             * - email
             * - sessionId
             */
            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(
                            userId,
                            email,
                            sessionId
                    );

            /*
             * Create Spring Security authentication.
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            AuthorityUtils.NO_AUTHORITIES
                    );

            /*
             * Store authentication in
             * Spring Security context.
             */
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (Exception e) {

            /*
             * Any of these can cause authentication
             * to fail:
             *
             * - Invalid JWT
             * - Expired JWT
             * - Invalid UUID
             * - Invalid claims
             * - Database/session error
             *
             * Don't authenticate the request.
             */
            SecurityContextHolder
                    .clearContext();
        }

        /*
         * Continue request processing.
         *
         * If authentication was successful,
         * Spring Security sees the request as
         * authenticated.
         *
         * Otherwise, if the endpoint requires
         * authentication, SecurityConfig will
         * return 401.
         */
        filterChain.doFilter(
                request,
                response
        );
    }
}