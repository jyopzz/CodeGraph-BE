package com.CodeGraph.auth.validation;

import com.CodeGraph.auth.dto.LoginRequest;
import com.CodeGraph.auth.dto.SendOtpRequest;
import com.CodeGraph.auth.dto.SetPasswordRequest;
import com.CodeGraph.auth.dto.VerifyOtpRequest;
import com.CodeGraph.auth.model.EmailVerification;
import com.CodeGraph.common.validation.BusinessAction;
import com.CodeGraph.common.validation.BusinessValidation;
import com.CodeGraph.common.validation.BusinessValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Component
public class AuthValidator
        implements BusinessValidator<EmailVerification> {

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final int MAX_PASSWORD_LENGTH = 100;

    private static final Pattern OTP_PATTERN =
            Pattern.compile("^\\d{6}$");

    @Override
    public BusinessValidation validate(
            BusinessAction action,
            EmailVerification existing,
            Object request) {

        BusinessValidation validation =
                new BusinessValidation();

        switch (action) {

            case SEND_OTP ->
                    validateSendOtp(
                            (SendOtpRequest) request,
                            validation
                    );

            case VERIFY_OTP ->
                    validateVerifyOtp(
                            (VerifyOtpRequest) request,
                            existing,
                            validation
                    );

            case SET_PASSWORD ->
                    validateSetPassword(
                            (SetPasswordRequest) request,
                            validation
                    );
            case LOGIN ->
                    validateLogin(
                            (LoginRequest) request,
                            validation
                    );

            default -> {
                // No authentication validation
            }
        }

        return validation;
    }

    private void validateSendOtp(
            SendOtpRequest request,
            BusinessValidation validation) {

        // Email
        if (request.email() == null
                || request.email().isBlank()) {

            validation.add(
                    AuthBusinessValidation.EMAIL_REQUIRED
            );

        } else if (!EMAIL_PATTERN.matcher(
                request.email()
        ).matches()) {

            validation.add(
                    AuthBusinessValidation.INVALID_EMAIL
            );
        }

    }

    private void validateVerifyOtp(
            VerifyOtpRequest request,
            EmailVerification existing,
            BusinessValidation validation) {

        // Email
        if (request.email() == null
                || request.email().isBlank()) {

            validation.add(
                    AuthBusinessValidation.EMAIL_REQUIRED
            );

        } else if (!EMAIL_PATTERN.matcher(
                request.email()
        ).matches()) {

            validation.add(
                    AuthBusinessValidation.INVALID_EMAIL
            );
        }

        // OTP
        if (request.otp() == null
                || request.otp().isBlank()) {

            validation.add(
                    AuthBusinessValidation.OTP_REQUIRED
            );

        } else if (!OTP_PATTERN.matcher(
                request.otp()
        ).matches()) {

            validation.add(
                    AuthBusinessValidation.INVALID_OTP
            );
        }

        // Don't check database state if request itself is invalid
        if (validation.hasErrors()) {
            return;
        }

        if (existing == null) {

            validation.add(
                    AuthBusinessValidation.OTP_NOT_FOUND
            );

            return;
        }

        if (existing.getVerifiedAt() != null) {

            validation.add(
                    AuthBusinessValidation.OTP_ALREADY_VERIFIED
            );

            return;
        }

        if (existing.getAttempts() >= MAX_OTP_ATTEMPTS) {

            validation.add(
                    AuthBusinessValidation.OTP_MAX_ATTEMPTS_EXCEEDED
            );

            return;
        }

        if (existing.getExpiresAt() == null
                || !existing.getExpiresAt()
                .isAfter(LocalDateTime.now())) {

            validation.add(
                    AuthBusinessValidation.OTP_EXPIRED
            );
        }
    }

    private void validateSetPassword(
            SetPasswordRequest request,
            BusinessValidation validation) {

        /*
         * Password
         */
        if (request.password() == null
                || request.password().isBlank()) {

            validation.add(
                    AuthBusinessValidation.PASSWORD_REQUIRED
            );

        } else {

            if (request.password().length()
                    < MIN_PASSWORD_LENGTH) {

                validation.add(
                        AuthBusinessValidation.PASSWORD_TOO_SHORT
                );
            }

            if (request.password().length()
                    > MAX_PASSWORD_LENGTH) {

                validation.add(
                        AuthBusinessValidation.PASSWORD_TOO_LONG
                );
            }
        }

        /*
         * Re-enter password
         */
        if (request.reenterPassword() == null
                || request.reenterPassword().isBlank()) {

            validation.add(
                    AuthBusinessValidation
                            .PASSWORD_CONFIRMATION_REQUIRED
            );

        } else if (request.password() != null
                && !request.password().equals(
                request.reenterPassword()
        )) {

            validation.add(
                    AuthBusinessValidation.PASSWORD_MISMATCH
            );
        }
    }

    private void validateLogin(
            LoginRequest request,
            BusinessValidation validation) {

        // Email
        validateEmail(
                request.email(),
                validation
        );

        // Password
        if (request.password() == null
                || request.password().isBlank()) {

            validation.add(
                    AuthBusinessValidation
                            .LOGIN_PASSWORD_REQUIRED
            );
        }
    }

    private void validateEmail(
            String email,
            BusinessValidation validation) {

        if (email == null
                || email.isBlank()) {

            validation.add(
                    AuthBusinessValidation.EMAIL_REQUIRED
            );

        } else if (!EMAIL_PATTERN.matcher(email).matches()) {

            validation.add(
                    AuthBusinessValidation.INVALID_EMAIL
            );
        }
    }
}