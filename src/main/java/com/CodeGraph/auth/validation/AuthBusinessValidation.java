package com.CodeGraph.auth.validation;

import com.CodeGraph.common.validation.BusinessValidationRule;
import com.CodeGraph.common.validation.ValidationInfo;
import com.CodeGraph.common.validation.ValidationSeverity;

public enum AuthBusinessValidation
        implements BusinessValidationRule {

    OTP_NOT_FOUND(
            "otp",
            "OTP_NOT_FOUND",
            "Verification code not found",
            ValidationSeverity.ERROR
    ),

    OTP_EXPIRED(
            "otp",
            "OTP_EXPIRED",
            "Verification code has expired",
            ValidationSeverity.ERROR
    ),

    OTP_ALREADY_VERIFIED(
            "otp",
            "OTP_ALREADY_VERIFIED",
            "Verification code has already been verified",
            ValidationSeverity.ERROR
    ),

    OTP_INVALID(
            "otp",
            "OTP_INVALID",
            "Invalid verification code",
            ValidationSeverity.ERROR
    ),
    EMAIL_REQUIRED(
            "email",
            "EMAIL_REQUIRED",
            "Email cannot be blank",
            ValidationSeverity.ERROR
    ),

    INVALID_EMAIL(
            "email",
            "INVALID_EMAIL",
            "Invalid email address",
            ValidationSeverity.ERROR
    ),

    OTP_REQUIRED(
            "otp",
            "OTP_REQUIRED",
            "OTP cannot be blank",
            ValidationSeverity.ERROR
    ),

    INVALID_OTP(
            "otp",
            "INVALID_OTP",
            "OTP must be exactly 6 digits",
            ValidationSeverity.ERROR
    ),

    OTP_MAX_ATTEMPTS_EXCEEDED(
            "otp",
            "OTP_MAX_ATTEMPTS_EXCEEDED",
            "Maximum OTP verification attempts exceeded",
            ValidationSeverity.ERROR
    ),
    REGISTRATION_TOKEN_REQUIRED(
            "authorization",
            "REGISTRATION_TOKEN_REQUIRED",
            "Registration token is required",
            ValidationSeverity.ERROR
    ),
    PASSWORD_REQUIRED(
            "password",
            "PASSWORD_REQUIRED",
            "Password cannot be blank",
            ValidationSeverity.ERROR
    ),

    PASSWORD_TOO_SHORT(
            "password",
            "PASSWORD_TOO_SHORT",
            "Password must contain at least 8 characters",
            ValidationSeverity.ERROR
    ),

    PASSWORD_TOO_LONG(
            "password",
            "PASSWORD_TOO_LONG",
            "Password cannot contain more than 100 characters",
            ValidationSeverity.ERROR
    ),

    PASSWORD_CONFIRMATION_REQUIRED(
            "reenterPassword",
            "PASSWORD_CONFIRMATION_REQUIRED",
            "Password confirmation cannot be blank",
            ValidationSeverity.ERROR
    ),

    PASSWORD_MISMATCH(
            "reenterPassword",
            "PASSWORD_MISMATCH",
            "Passwords do not match",
            ValidationSeverity.ERROR
    ),
    INVALID_REGISTRATION_TOKEN(
            "authorization",
            "INVALID_REGISTRATION_TOKEN",
            "Invalid or expired registration token",
            ValidationSeverity.ERROR
    ),

    USER_ALREADY_EXISTS(
            "email",
            "USER_ALREADY_EXISTS",
            "A user with this email already exists",
            ValidationSeverity.ERROR
    ),
    INVALID_CREDENTIALS(
            "login",
            "INVALID_CREDENTIALS",
            "Invalid email or password",
            ValidationSeverity.ERROR
    ),
    LOGIN_PASSWORD_REQUIRED(
            "password",
            "LOGIN_PASSWORD_REQUIRED",
            "Password cannot be blank",
            ValidationSeverity.ERROR
    ),
    INVALID_REFRESH_TOKEN(
            "refreshToken",
            "INVALID_REFRESH_TOKEN",
            "Invalid or expired refresh token",
            ValidationSeverity.ERROR
    ),;


    private final ValidationInfo info;

    AuthBusinessValidation(
            String field,
            String code,
            String message,
            ValidationSeverity severity) {

        this.info = new ValidationInfo(
                field,
                code,
                message,
                severity
        );
    }

    @Override
    public ValidationInfo info() {
        return info;
    }
}