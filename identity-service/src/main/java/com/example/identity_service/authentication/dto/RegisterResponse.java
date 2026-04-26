package com.example.identity_service.authentication.dto;

import com.example.identity_service.user.dto.UserDto;

public record RegisterResponse (String token, String refreshToken, UserDto user) { }
