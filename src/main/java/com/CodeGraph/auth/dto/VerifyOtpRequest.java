package com.CodeGraph.auth.dto;

public record VerifyOtpRequest(
        String email,
        String otp
) {
}