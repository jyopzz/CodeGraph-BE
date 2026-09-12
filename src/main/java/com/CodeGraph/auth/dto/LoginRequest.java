package com.CodeGraph.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}