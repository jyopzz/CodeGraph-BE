package com.CodeGraph.auth.util;

public final class AuthorizationUtils {

    private AuthorizationUtils() {
    }

    public static String extractBearerToken(
            String authorization) {

        if (authorization == null
                || authorization.isBlank()) {

            return null;
        }

        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        String token =
                authorization.substring(7).trim();

        return token.isBlank() ? null : token;
    }
}