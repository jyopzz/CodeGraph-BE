package com.CodeGraph.auth.controller;

import com.CodeGraph.auth.dto.*;
import com.CodeGraph.auth.security.AuthenticatedUser;
import com.CodeGraph.auth.service.AuthService;
import com.CodeGraph.auth.validation.AuthBusinessValidation;
import com.CodeGraph.common.response.BaseResponse;
import com.CodeGraph.common.validation.BusinessValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<BaseResponse<Void>> sendRegistrationOtp(
            @Valid @RequestBody SendOtpRequest request) {

        BaseResponse<Void> response = authService.sendRegistrationOtp(request);

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @PostMapping("/register/verify-otp")
    @Operation(
            summary = "Verify registration OTP",
            description = "Verifies OTP and sets registration_token in an HttpOnly cookie."
    )
    public ResponseEntity<BaseResponse<Void>> verifyRegistrationOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletResponse httpResponse) {

        BaseResponse<VerifyOtpResponse> response = authService.verifyRegistrationOtp(request);

        if (response.isSuccessful() && response.getData() != null) {
            attachRegistrationCookie(response.getData().token(), httpResponse);
        }

        BaseResponse<Void> responses = new BaseResponse<>(
                null,
                response.getMessage(),
                response.getBusinessValidation(),
                response.isSuccessful(),
                response.getResponseCode()
        );

        return ResponseEntity
                .status(response.getResponseCode())
                .body(responses);
    }

    @PostMapping("/register/set-password")
    @Operation(
            summary = "Set password and create user",
            description = """
                    Creates a user account using a valid registration token.
                    The registration token is extracted automatically from the HttpOnly cookie.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Password validation failed"),
            @ApiResponse(responseCode = "403", description = "Registration token is missing, invalid or expired"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<LoginResponse>> setPassword(

            @CookieValue(name = "registration_token", required = false)
            String registrationToken,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password information",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SetPasswordRequest.class),
                            examples = @ExampleObject(
                                    name = "Set Password",
                                    value = """
                                            {
                                              "password": "MyPassword123!",
                                              "reenterPassword": "MyPassword123!"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody SetPasswordRequest request,

            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (registrationToken == null || registrationToken.isBlank()) {
            BusinessValidation validation = new BusinessValidation();
            validation.add(AuthBusinessValidation.REGISTRATION_TOKEN_REQUIRED);

            BaseResponse<LoginResponse> errorResponse = new BaseResponse<>(
                    null,
                    "Registration token is missing or expired",
                    validation,
                    false,
                    403
            );
            return ResponseEntity.status(403).body(errorResponse);
        }

        String clientIp = httpRequest.getRemoteAddr();

        BaseResponse<LoginResponse> response = authService.setPassword(
                registrationToken,
                request,
                clientIp
        );

        if (response.isSuccessful() && response.getData() != null) {
            // Attach active session tokens (access_token & refresh_token)
            attachTokenCookies(response.getData(), httpResponse);
            // Expire registration cookie since it has been consumed
            clearRegistrationCookie(httpResponse);
            response.setData(new LoginResponse(null, null));
        }

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = """
                Authenticates a user using email and password.
                Returns tokens via HttpOnly cookies and strips them from body.
                Every login attempt is recorded with the client IP address.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<LoginResponse>> login(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login",
                                    value = """
                                        {
                                          "email": "test@gmail.com",
                                          "password": "MyPassword123!"
                                        }
                                        """
                            )
                    )
            )
            @RequestBody LoginRequest request,

            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String clientIp = httpRequest.getRemoteAddr();

        BaseResponse<LoginResponse> response = authService.login(
                request,
                clientIp
        );

        if (response.isSuccessful() && response.getData() != null) {
            attachTokenCookies(response.getData(), httpResponse);
            response.setData(new LoginResponse(null, null));
        }

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = """
                Refreshes the access token using the refresh_token HttpOnly cookie.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing refresh token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<LoginResponse>> refreshAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse httpResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            BusinessValidation validation = new BusinessValidation();
            validation.add(AuthBusinessValidation.INVALID_REFRESH_TOKEN);

            BaseResponse<LoginResponse> errorResponse = new BaseResponse<>(
                    null,
                    "Token refresh failed",
                    validation,
                    false,
                    401
            );
            return ResponseEntity.status(401).body(errorResponse);
        }

        BaseResponse<LoginResponse> response = authService.refreshAccessToken(
                new RefreshTokenRequest(refreshToken)
        );

        if (response.isSuccessful() && response.getData() != null) {
            attachTokenCookies(response.getData(), httpResponse);
            response.setData(new LoginResponse(null, null));
        }

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<AuthenticatedUser>> getCurrentUser(Authentication authentication) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();

        BaseResponse<AuthenticatedUser> response =
                BaseResponse.success(authUser, "Authenticated successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = """
                    Terminates the user session, clears the refresh token mapping,
                    and expires authentication cookies.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Void>> logout(
            Authentication authentication,
            HttpServletResponse httpResponse) {

        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            authService.logout(authUser.sessionId());
        }

        clearTokenCookies(httpResponse);

        BaseResponse<Void> response = BaseResponse.success(null, "Logged out successfully");

        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // COOKIE HELPERS
    // -------------------------------------------------------------------------

    private void attachRegistrationCookie(String registrationToken, HttpServletResponse httpResponse) {
        ResponseCookie cookie = ResponseCookie.from("registration_token", registrationToken)
                .httpOnly(true)
                .secure(false) // Set to true in production under HTTPS
                .path("/api/auth/register") // Scoped to register endpoints
                .sameSite("Lax")
                .maxAge(15 * 60) // 15 minutes window to complete password creation
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRegistrationCookie(HttpServletResponse httpResponse) {
        ResponseCookie cookie = ResponseCookie.from("registration_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/register")
                .sameSite("Lax")
                .maxAge(0) // Expire immediately
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void attachTokenCookies(LoginResponse loginData, HttpServletResponse httpResponse) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", loginData.accessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(15 * 60)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", loginData.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    private void clearTokenCookies(HttpServletResponse httpResponse) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}