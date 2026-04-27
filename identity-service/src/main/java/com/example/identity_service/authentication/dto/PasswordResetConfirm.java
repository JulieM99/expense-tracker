package com.example.identity_service.authentication.dto;

public record PasswordResetConfirm(String token, String newPassword) {}
