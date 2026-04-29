package com.example.identity_service;

import com.example.identity_service.authentication.Role;
import com.example.identity_service.authentication.Token;
import com.example.identity_service.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestData {

    private final PasswordEncoder passwordEncoder;

    public TestData(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User.UserBuilder userBuilder() {
        return User.builder()
                .email("email@test.pl")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.ROLE_USER)
                .firstName("John")
                .firstName("John")
                .lastName("Doe")
                .isActive(true);
    }

    public Token.TokenBuilder tokenBuilder() {
        String randomToken = java.util.UUID.randomUUID().toString();
        return Token.builder()
                .user(null)
                .refreshToken(randomToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(java.time.LocalDateTime.now().plusDays(7))
                .revoked(false);
    }
}