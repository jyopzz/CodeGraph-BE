package com.CodeGraph.auth.service;

import com.CodeGraph.auth.dto.*;
import com.CodeGraph.auth.enums.OtpPurpose;
import com.CodeGraph.auth.mapper.AuthSessionMapper;
import com.CodeGraph.auth.mapper.EmailVerificationMapper;
import com.CodeGraph.auth.mapper.LoginHistoryMapper;
import com.CodeGraph.auth.model.AuthSession;
import com.CodeGraph.auth.model.EmailVerification;
import com.CodeGraph.auth.model.LoginHistory;
import com.CodeGraph.auth.model.RefreshToken;
import com.CodeGraph.auth.validation.AuthBusinessValidation;
import com.CodeGraph.auth.validation.AuthValidator;
import com.CodeGraph.common.exception.BusinessException;
import com.CodeGraph.common.response.BaseResponse;
import com.CodeGraph.common.validation.BusinessAction;
import com.CodeGraph.common.validation.BusinessValidation;
import com.CodeGraph.notification.email.EmailService;
import com.CodeGraph.user.mapper.UserMapper;
import com.CodeGraph.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final int OTP_EXPIRATION_MINUTES = 5;

    private final EmailVerificationMapper emailVerificationMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthValidator authValidator;
    private final RegistrationTokenService registrationTokenService;
    private final UserMapper userMapper;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final LoginHistoryMapper loginHistoryMapper;
    private final AuthSessionMapper authSessionMapper;
    private final long refreshExpirationDays;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public AuthService(
            EmailVerificationMapper emailVerificationMapper,
            UserMapper userMapper,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            AuthValidator authValidator,
            RegistrationTokenService registrationTokenService,
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService,
            LoginHistoryMapper loginHistoryMapper,
            AuthSessionMapper authSessionMapper,
            @Value("${koode.jwt.refresh-expiration-days}")
            long refreshExpirationDays) {

        this.emailVerificationMapper =
                emailVerificationMapper;

        this.userMapper =
                userMapper;

        this.emailService =
                emailService;

        this.passwordEncoder =
                passwordEncoder;

        this.authValidator =
                authValidator;

        this.registrationTokenService =
                registrationTokenService;

        this.accessTokenService =
                accessTokenService;

        this.refreshTokenService =
                refreshTokenService;

        this.loginHistoryMapper =
                loginHistoryMapper;

        this.authSessionMapper =
                authSessionMapper;

        this.refreshExpirationDays =
                refreshExpirationDays;
    }

    @Transactional
    public BaseResponse<Void> sendRegistrationOtp(
            SendOtpRequest request) {

        String email =
                normalizeEmail(request.email());



        BusinessValidation validation =
                authValidator.validate(
                        BusinessAction.SEND_OTP,
                        null,
                        request
                );

        if (validation.hasErrors()) {

            return new BaseResponse<>(
                    null,
                    "Verification code could not be sent",
                    validation,
                    false,
                    400
            );
        }
        User existingUser =
                userMapper.findByEmail(email);

        if (existingUser != null) {

            validation.add(
                    AuthBusinessValidation
                            .USER_ALREADY_EXISTS
            );

            return new BaseResponse<>(
                    null,
                    "Verification code could not be sent",
                    validation,
                    false,
                    409
            );
        }

        emailVerificationMapper.invalidateActiveOtps(
                email,
                OtpPurpose.REGISTER.name()
        );

        String otp = generateOtp();

        String otpHash =
                passwordEncoder.encode(otp);

        EmailVerification verification =
                new EmailVerification();

        verification.setEmail(email);
        verification.setOtpHash(otpHash);
        verification.setPurpose(
                OtpPurpose.REGISTER
        );
        verification.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(
                                OTP_EXPIRATION_MINUTES
                        )
        );
        verification.setAttempts(0);

        emailVerificationMapper.save(
                verification
        );

        emailService.sendOtp(
                email,
                otp
        );

        return new BaseResponse<>(
                null,
                "Verification code sent successfully",
                validation,
                true,
                200
        );
    }

    @Transactional
    public BaseResponse<VerifyOtpResponse> verifyRegistrationOtp(
            VerifyOtpRequest request) {

        String email =
                normalizeEmail(request.email());

        EmailVerification verification =
                emailVerificationMapper.findLatest(
                        email,
                        OtpPurpose.REGISTER.name()
                );

        /*
         * Business validation
         */
        BusinessValidation validation =
                authValidator.validate(
                        BusinessAction.VERIFY_OTP,
                        verification,
                        request
                );

        if (validation.hasErrors()) {

            return new BaseResponse<>(
                    null,
                    "OTP verification failed",
                    validation,
                    false,
                    400
            );
        }

        /*
         * Compare submitted OTP with stored hash.
         */
        boolean validOtp =
                passwordEncoder.matches(
                        request.otp(),
                        verification.getOtpHash()
                );

        /*
         * Invalid OTP
         */
        if (!validOtp) {

            emailVerificationMapper.incrementAttempts(
                    verification.getId()
            );

            validation.add(
                    AuthBusinessValidation.OTP_INVALID
            );

            return new BaseResponse<>(
                    null,
                    "OTP verification failed",
                    validation,
                    false,
                    400
            );
        }

        /*
         * Mark OTP as verified.
         *
         * The mapper uses:
         *
         * WHERE id = #{id}
         * AND verified_at IS NULL
         */
        int updatedRows =
                emailVerificationMapper.markVerified(
                        verification.getId()
                );
        /*
         * Prevent the same OTP from being
         * consumed twice concurrently.
         */
        if (updatedRows != 1) {

            validation.add(
                    AuthBusinessValidation
                            .OTP_ALREADY_VERIFIED
            );

            return new BaseResponse<>(
                    null,
                    "OTP verification failed",
                    validation,
                    false,
                    400
            );
        }
        String registrationToken =
                registrationTokenService.generateToken(email);

        return new BaseResponse<>(
                new VerifyOtpResponse("registration_token",
                        registrationToken),
                "Email verified successfully",
                validation,
                true,
                200
        );
    }

    private String generateOtp() {

        int otp =
                secureRandom.nextInt(1_000_000);

        return String.format(
                "%06d",
                otp
        );
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase();
    }

    @Transactional
    public BaseResponse<LoginResponse> setPassword(
            String registrationToken,
            SetPasswordRequest request,
            String clientIp) {

        if (registrationToken == null || registrationToken.isBlank()) {
            BusinessValidation validation = new BusinessValidation();
            validation.add(AuthBusinessValidation.REGISTRATION_TOKEN_REQUIRED);

            return new BaseResponse<LoginResponse>(
                    null,
                    "Set password failed",
                    validation,
                    false,
                    400
            );
        }

        /*
         * Password validation
         */
        BusinessValidation validation =
                authValidator.validate(
                        BusinessAction.SET_PASSWORD,
                        null,
                        request
                );

        if (validation.hasErrors()) {
            return new BaseResponse<LoginResponse>(
                    null,
                    "Set password failed",
                    validation,
                    false,
                    400
            );
        }

        String email;
        try {
            email = normalizeEmail(
                    registrationTokenService.validateAndGetEmail(registrationToken)
            );
        } catch (Exception e) {
            validation.add(AuthBusinessValidation.INVALID_REGISTRATION_TOKEN);
            throw new BusinessException(403, validation);
        }

        User existingUser = userMapper.findByEmail(email);
        if (existingUser != null) {
            validation.add(AuthBusinessValidation.USER_ALREADY_EXISTS);
            throw new BusinessException(409, validation);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedIp(clientIp);

        userMapper.insert(user);

        saveLoginHistory(
                user.getId(),
                user.getEmail(),
                clientIp,
                true
        );

        UUID sessionId = UUID.randomUUID();
        UUID accessTokenId = UUID.randomUUID();

        AuthSession session = new AuthSession();
        session.setUserId(user.getId());
        session.setSessionId(sessionId);
        session.setAccessTokenId(accessTokenId);
        session.setCreatedAt(LocalDateTime.now());
        session.setCreatedIp(clientIp);
        session.setExpiresAt(LocalDateTime.now().plusDays(refreshExpirationDays));

        authSessionMapper.insert(session);

        String accessToken = accessTokenService.generateToken(user, sessionId, accessTokenId);
        String refreshToken = refreshTokenService.generateToken(user, sessionId);
        refreshTokenService.save(user, refreshToken, clientIp);

        LoginResponse response = new LoginResponse(accessToken, refreshToken);

        return new BaseResponse<LoginResponse>(
                response,
                "Account created successfully",
                validation,
                true,
                201
        );
    }

    @Transactional
    public BaseResponse<LoginResponse> login(
            LoginRequest request,
            String clientIp) {

        /*
         * Normalize email
         */
        String email =
                normalizeEmail(request.email());

        /*
         * Business validation
         */
        BusinessValidation validation =
                authValidator.validate(
                        BusinessAction.LOGIN,
                        null,
                        request
                );

        if (validation.hasErrors()) {

            saveLoginHistory(
                    null,
                    email,
                    clientIp,
                    false
            );

            return new BaseResponse<>(
                    null,
                    "Login failed",
                    validation,
                    false,
                    400
            );
        }

        /*
         * Find user
         */
        User user =
                userMapper.findByEmail(email);

        /*
         * Check credentials
         */
        boolean validCredentials =
                user != null
                        && passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                );

        /*
         * Record EVERY login attempt
         */
        saveLoginHistory(
                user != null
                        ? user.getId()
                        : null,
                email,
                clientIp,
                validCredentials
        );

        /*
         * Invalid credentials
         */
        if (!validCredentials) {

            validation.add(
                    AuthBusinessValidation
                            .INVALID_CREDENTIALS
            );

            return new BaseResponse<>(
                    null,
                    "Login failed",
                    validation,
                    false,
                    401
            );
        }

        UUID sessionId =
                UUID.randomUUID();

        UUID accessTokenId =
                UUID.randomUUID();

        AuthSession session =
                new AuthSession();

        session.setUserId(
                user.getId()
        );

        session.setSessionId(
                sessionId
        );

        session.setAccessTokenId(
                accessTokenId
        );

        session.setCreatedAt(
                LocalDateTime.now()
        );

        session.setCreatedIp(
                clientIp
        );

        session.setExpiresAt(
                LocalDateTime.now()
                        .plusDays(refreshExpirationDays)
        );

        authSessionMapper.insert(session);

        /*
         * Generate tokens for this session
         */
        String accessToken =
                accessTokenService.generateToken(
                        user,
                        sessionId,
                        accessTokenId
                );

        String refreshToken =
                refreshTokenService.generateToken(
                        user,
                        sessionId
                );

        /*
         * Save refresh token
         */
        refreshTokenService.save(
                user,
                refreshToken,
                clientIp
        );

        /*
         * Response
         */
        LoginResponse response =
                new LoginResponse(
                        accessToken,
                        refreshToken
                );

        return new BaseResponse<>(
                response,
                "Login successful",
                validation,
                true,
                200
        );
    }

    private void saveLoginHistory(
            Long userId,
            String email,
            String clientIp,
            boolean successful) {

        LoginHistory history =
                new LoginHistory();

        history.setUserId(userId);
        history.setEmail(email);
        history.setIpAddress(clientIp);
        history.setAttemptedAt(
                LocalDateTime.now()
        );
        history.setSuccessful(successful);

        loginHistoryMapper.insert(history);
    }


    @Transactional
    public BaseResponse<LoginResponse> refreshAccessToken(
            RefreshTokenRequest request) {

        BusinessValidation validation =
                new BusinessValidation();

        try {

            /*
             * Validate refresh token
             */
            RefreshToken storedToken =
                    refreshTokenService.validateAndGetEntity(
                            request.refreshToken()
                    );

            /*
             * Find user
             */
            User user =
                    userMapper.findById(
                            storedToken.getUserId()
                    );

            if (user == null) {

                validation.add(
                        AuthBusinessValidation
                                .INVALID_REFRESH_TOKEN
                );

                return new BaseResponse<>(
                        null,
                        "Token refresh failed",
                        validation,
                        false,
                        401
                );
            }

            /*
             * Generate new access token
             */
            UUID newAccessTokenId =
                    UUID.randomUUID();

            int updatedRows =
                    authSessionMapper.updateAccessTokenId(
                            storedToken.getSessionId(),
                            newAccessTokenId
                    );

            if (updatedRows != 1) {

                validation.add(
                        AuthBusinessValidation
                                .INVALID_REFRESH_TOKEN
                );

                return new BaseResponse<>(
                        null,
                        "Token refresh failed",
                        validation,
                        false,
                        401
                );
            }


            String accessToken =
                    accessTokenService.generateToken(
                            user,
                            storedToken.getSessionId(),
                            newAccessTokenId
                    );

            /*
             * Return new access token
             * with existing refresh token
             */
            LoginResponse response =
                    new LoginResponse(
                            accessToken,
                            request.refreshToken()
                    );

            return new BaseResponse<>(
                    response,
                    "Access token refreshed successfully",
                    validation,
                    true,
                    200
            );

        } catch (Exception e) {

            validation.add(
                    AuthBusinessValidation
                            .INVALID_REFRESH_TOKEN
            );

            return new BaseResponse<>(
                    null,
                    "Token refresh failed",
                    validation,
                    false,
                    401
            );
        }
    }

    @Transactional
    public void logout(UUID sessionId) {
        if (sessionId != null) {
            refreshTokenService.revokeBySessionId(sessionId);
            authSessionMapper.revoke(sessionId);
        }
    }
}