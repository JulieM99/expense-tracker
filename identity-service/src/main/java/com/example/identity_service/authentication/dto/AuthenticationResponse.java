package com.example.identity_service.authentication.dto;


public record AuthenticationResponse (String token, String refreshToken, UserDto user) { }
