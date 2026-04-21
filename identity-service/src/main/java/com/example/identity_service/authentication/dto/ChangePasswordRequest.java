package com.example.identity_service.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Request for changing user password")
public record ChangePasswordRequest(

        @NotBlank(message = "Old password is required")
        @Schema(example = "oldPassword123")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        @Schema(example = "newSecurePassword123")
        String newPassword

) {}