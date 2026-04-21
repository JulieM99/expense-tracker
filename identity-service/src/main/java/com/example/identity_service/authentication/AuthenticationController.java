package com.example.identity_service.authentication;

import com.example.identity_service.authentication.dto.*;
import com.example.identity_service.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    public static final String AUTHENTICATION_PATH = "/api/auth";
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

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

    @Operation(
            summary = "Get current authenticated user",
            description = "Returns information about the currently authenticated user based on the provided JWT access token.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully retrieved",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing or invalid JWT token",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        return ResponseEntity.ok(authenticationService.getCurrentUser(authentication));
    }

    @Operation(
            summary = "Change user password",
            description = "Allows authenticated user to change their password by providing old and new password. Requires valid JWT access token.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Password successfully changed"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid or expired token / wrong old password",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        User user = (User) authentication.getPrincipal();

        authenticationService.changePassword(user, request);

        return ResponseEntity.noContent().build();
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
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.token());
        return ResponseEntity.noContent().build();
    }

}
