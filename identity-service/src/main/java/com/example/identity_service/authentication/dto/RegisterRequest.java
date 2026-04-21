package com.example.identity_service.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Request for user registration")
public record RegisterRequest (

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Schema(example = "user@example.com")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(example = "securePassword123")
    String password,

    @NotBlank(message = "First name is required")
    @Schema(example = "John")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Schema(example = "Doe")
    String lastName

) {}