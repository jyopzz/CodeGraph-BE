package com.CodeGraph.auth.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public record VerifyOtpResponse(
        String tokenName,
        String token
) {

    @JsonValue
    public Map<String, String> tokenData() {
        return Map.of(tokenName, token);
    }
}