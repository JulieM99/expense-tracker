package com.example.identity_service.authentication;

import com.example.identity_service.authentication.dto.AuthenticationRequest;
import com.example.identity_service.authentication.dto.AuthenticationResponse;
import com.example.identity_service.authentication.dto.RegisterRequest;
import com.example.identity_service.authentication.dto.RegisterResponse;
import com.example.identity_service.error.exception.UnauthorizedException;
import com.example.identity_service.user.User;
import com.example.identity_service.user.UserMapper;
import com.example.identity_service.user.UserRepository;
import com.example.identity_service.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private AuthenticationService authenticationService;


    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = mock(RegisterRequest.class);
        when(request.email()).thenReturn("test@test.com");
        when(request.password()).thenReturn("pass");

        User user = new User();
        user.setEmail("test@test.com");

        UserDto userDto = mock(UserDto.class);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("jwt", response.token());
        assertEquals(userDto, response.user());

        verify(userRepository).save(user);

    }

    @Test
    void shouldAuthenticateUserSuccessfully() throws Exception {
        AuthenticationRequest request = mock(AuthenticationRequest.class);
        when(request.email()).thenReturn("test@test.com");
        when(request.password()).thenReturn("pass");

        User user = new User();
        user.setEmail("test@test.com");

        UserDto dto = mock(UserDto.class);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(userMapper.toDto(user)).thenReturn(dto);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt", response.token());
        assertEquals(dto, response.user());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationFails() {

        AuthenticationRequest request = mock(AuthenticationRequest.class);
        when(request.email()).thenReturn("test@test.com");
        when(request.password()).thenReturn("pass");

        doThrow(new RuntimeException("bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(UnauthorizedException.class,
                () -> authenticationService.authenticate(request));
    }

    @Test
    void shouldRefreshTokenSuccessfully() {

        User user = new User();
        user.setEmail("test@test.com");

        Token token = Token.builder()
                .refreshToken("refresh")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        UserDto dto = mock(UserDto.class);

        when(tokenRepository.findByRefreshToken("refresh"))
                .thenReturn(Optional.of(token));

        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(userMapper.toDto(user)).thenReturn(dto);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthenticationResponse response =
                authenticationService.refreshToken("refresh");

        assertNotNull(response);
        assertEquals("jwt", response.token());
        assertEquals(dto, response.user());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenFails() {

        when(tokenRepository.findByRefreshToken("bad"))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authenticationService.refreshToken("bad"));
    }
}
