package com.example.identity_service.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Request for changing user data")
public record UpdateUserRequest(

        @NotBlank(message = "firstName is required")
        @Schema(example = "Julia")
        String firstName,

        @NotBlank(message = "lastName is required")
        @Schema(example = "Kowalska")
        String lastName
) {
}
