package com.example.identity_service.authentication;

import com.example.identity_service.authentication.dto.*;
import com.example.identity_service.error.ApiError;
import com.example.identity_service.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    public static final String AUTHENTICATION_PATH = "/api/auth";
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns initial authentication tokens (JWT access token and refresh token).",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully registered and authenticated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RegisterResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Invalid request data or user already exists",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

        var response = authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user credentials and returns a short-lived JWT access token and a refresh token.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthenticationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid email or password",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {

        AuthenticationResponse response = authenticationService.authenticate(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Request password reset",
            description = "Sends password reset email with a one-time token link. User does not need to be authenticated.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Reset email sent (if user exists)"
                    )
            }
    )
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestReset(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {

        authenticationService.requestReset(passwordResetRequest);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Confirm password reset",
            description = "Resets user password using valid reset token from email link.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Password successfully reset"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or expired token",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid PasswordResetConfirm passwordResetConfirm) {

        authenticationService.resetPassword(passwordResetConfirm);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new JWT access token and refresh token using a valid refresh token. The previous refresh token is invalidated.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Token refresh successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthenticationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid or expired refresh token",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request.token()));
    }

    @Operation(
            summary = "Logout user",
            description = "Revokes the provided refresh token, terminating the user's session. The current JWT remains valid until expiration.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Logout successful (refresh token revoked)"
                    )
            }
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        authenticationService.logout(user);
        return ResponseEntity.noContent().build();
    }

}
