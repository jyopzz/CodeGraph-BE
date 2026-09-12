package com.CodeGraph.auth.dto;

public record SetPasswordRequest(
        String password,
        String reenterPassword
) {
}