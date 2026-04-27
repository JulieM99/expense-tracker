package com.example.identity_service.authentication;

import com.example.identity_service.authentication.dto.*;
import com.example.identity_service.common.event.PasswordResetRequestedEvent;
import com.example.identity_service.common.event.UserRegisteredEvent;
import com.example.identity_service.error.exception.ConflictException;
import com.example.identity_service.error.exception.NotFoundException;
import com.example.identity_service.error.exception.UnauthorizedException;
import com.example.identity_service.user.User;
import com.example.identity_service.user.UserMapper;
import com.example.identity_service.user.UserRepository;
import com.example.identity_service.user.dto.UserDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

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

        eventPublisher.publishEvent(
                new UserRegisteredEvent(user.getEmail(), user.getFirstName())
        );

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

    @Transactional
    public void requestReset(@Valid PasswordResetRequest passwordResetRequest) {

        log.info("AUTHENTICATION SERVICE reset password request for email={}", passwordResetRequest.email());

        User user = userRepository.findByEmail(passwordResetRequest.email())
                .orElse(null);

        if (user == null) {
            return;
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        eventPublisher.publishEvent(
                new PasswordResetRequestedEvent(
                        user.getEmail(),
                        user.getFirstName(),
                        token
                )
        );
    }

    @Transactional
    public void resetPassword(@Valid PasswordResetConfirm request) {

        PasswordResetToken token = passwordResetTokenRepository
                .findByToken(request.token())
                .orElseThrow(() -> new NotFoundException("Reset token not found"));

        if (token.isUsed()) {
            throw new ConflictException("Reset token already used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = token.getUser();

        if (user == null) {
            throw new NotFoundException("User not found for token");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        //token zuzyty!
        token.setUsed(true);

        userRepository.save(user);
        passwordResetTokenRepository.save(token);

        log.info("AUTH SERVICE reset password confirmed");
    }

}
