package com.example.identity_service.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Request to authenticate user")
public record AuthenticationRequest (

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email required")
    String email,

    @NotBlank(message = "Password required")
    String password
) {}
