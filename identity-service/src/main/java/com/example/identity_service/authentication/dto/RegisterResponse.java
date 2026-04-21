package com.example.identity_service.authentication.dto;

public record RegisterResponse (String token, String refreshToken, UserDto user) { }
