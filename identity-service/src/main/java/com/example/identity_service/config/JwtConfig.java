package com.example.identity_service.config;

import org.example.authcommon.JwtProperties;
import org.example.authcommon.JwtService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties.getSecret());
    }
}