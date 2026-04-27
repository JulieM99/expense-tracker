package com.example.identity_service.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Request to reset user password")
public record PasswordResetRequest(

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email required")
    String email
) {
}
