package com.example.identity_service.common.event;

public record UserRegisteredEvent(String email, String firstName) {}