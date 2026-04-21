package com.example.identity_service.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Request containing refresh token")
public record RefreshTokenRequest(

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    String token
) {}