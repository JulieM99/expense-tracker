package com.example.identity_service.authentication;

import com.example.identity_service.authentication.dto.*;
import com.example.identity_service.error.exception.ConflictException;
import com.example.identity_service.error.exception.UnauthorizedException;
import com.example.identity_service.user.User;
import com.example.identity_service.user.UserMapper;
import com.example.identity_service.user.UserRepository;
import com.example.identity_service.user.dto.ChangePasswordRequest;
import com.example.identity_service.user.dto.UserDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(@Valid RegisterRequest request)  {

        log.info("AUTHENTICATION SERVICE register start email={}", request.email());

        userRepository.findByEmail(request.email())
                .ifPresent(u -> {
                    log.warn("AUTHENTICATION SERVICE register FAILED (email exists) email={}", request.email());
                    throw new ConflictException("User with this e-mail already exists");
                });

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = saveRefreshToken(user);

        UserDto userDto = userMapper.toDto(user);

        log.info("AUTHENTICATION SERVICE user created id={} email={}", user.getId(), user.getEmail());

        return new RegisterResponse(jwtToken, refreshToken, userDto);
    }

    @Transactional
    public AuthenticationResponse authenticate(@Valid AuthenticationRequest request) {

        log.info("AUTHENTICATION SERVICE login attempt email={}", request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception e) {
            log.warn("AUTHENTICATION SERVICE login FAILED email={}", request.email());
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        log.info("AUTHENTICATION SERVICE login SUCCESS email={}", request.email());

        return new AuthenticationResponse(
                jwtService.generateToken(user),
                saveRefreshToken(user),
                userMapper.toDto(user)
        );
    }

    @Transactional
    public AuthenticationResponse refreshToken(String token) {

        log.info("AUTHENTICATION SERVICE refresh attempt");

        Token storedToken = tokenRepository.findByRefreshToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = storedToken.getUser();

        return new AuthenticationResponse(
                jwtService.generateToken(user),
                saveRefreshToken(user),
                userMapper.toDto(user)
        );
    }

    public String saveRefreshToken(User user) {

        tokenRepository.deleteAllByUser(user);

        String randomToken = java.util.UUID.randomUUID().toString();

        Token refreshToken = Token.builder()
                .user(user)
                .refreshToken(randomToken)
                .createdAt(java.time.LocalDateTime.now())
                .expiresAt(java.time.LocalDateTime.now().plusDays(7)) //7 dni zycia refresh tokena
                .revoked(false)
                .build();

        tokenRepository.save(refreshToken);
        return randomToken;
    }

    @Transactional
    public void logout(String refreshToken) {

        log.info("AUTHENTICATION SERVICE logout");

        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        tokenRepository.delete(token);
    }

}
