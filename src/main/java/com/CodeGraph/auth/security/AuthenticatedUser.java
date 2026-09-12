package com.CodeGraph.auth.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record AuthenticatedUser(
        Long userId,
        String email,
        @JsonIgnore
        UUID sessionId
) {
}