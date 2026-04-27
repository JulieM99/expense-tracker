package com.example.identity_service.common.event;

public record PasswordResetRequestedEvent(
        String email,
        String firstName,
        String token
) {}
