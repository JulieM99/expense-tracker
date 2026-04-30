package com.example.budget_service.config.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record UserPrincipal(
        Long id,
        String email,
        Collection<? extends GrantedAuthority> authorities
) {}
