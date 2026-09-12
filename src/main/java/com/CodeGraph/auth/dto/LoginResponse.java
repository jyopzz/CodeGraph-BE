package com.CodeGraph.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}