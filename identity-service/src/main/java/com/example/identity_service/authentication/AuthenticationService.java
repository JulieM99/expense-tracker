package com.example.identity_service.authentication;

import com.example.identity_service.authentication.dto.*;
import com.example.identity_service.error.exception.ConflictException;
import com.example.identity_service.error.exception.UnauthorizedException;
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

        log.info("SERVICE register start email={}", request.email());

        userRepository.findByEmail(request.email())
                .ifPresent(u -> {
                    log.warn("SERVICE register FAILED (email exists) email={}", request.email());
                    throw new ConflictException("User with this e-mail already exists");
                });

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = saveRefreshToken(user);

        UserDto userDto = userMapper.toDto(user);

        log.info("SERVICE user created id={} email={}", user.getId(), user.getEmail());

        return new RegisterResponse(jwtToken, refreshToken, userDto);
    }

    public UserDto getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toDto(user);
    }

    @Transactional
    public AuthenticationResponse authenticate(@Valid AuthenticationRequest request) {

        log.info("SERVICE login attempt email={}", request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception e) {
            log.warn("SERVICE login FAILED email={}", request.email());
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        log.info("SERVICE login SUCCESS email={}", request.email());

        return new AuthenticationResponse(
                jwtService.generateToken(user),
                saveRefreshToken(user),
                userMapper.toDto(user)
        );
    }

    @Transactional
    public AuthenticationResponse refreshToken(String token) {

        log.info("SERVICE refresh attempt");

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

        log.info("SERVICE logout");

        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        tokenRepository.delete(token);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {

        log.info("SERVICE changePassword userId={}", user.getId());

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

}
