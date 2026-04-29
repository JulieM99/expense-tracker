package com.example.identity_service.user;

import com.example.identity_service.common.event.UserDeleteEvent;
import com.example.identity_service.error.exception.NotFoundException;
import com.example.identity_service.error.exception.UnauthorizedException;
import com.example.identity_service.user.dto.ChangePasswordRequest;
import com.example.identity_service.user.dto.UpdateUserRequest;
import com.example.identity_service.user.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnCurrentUser() {

        User user = new User();
        user.setEmail("test@test.com");

        UserDto dto = mock(UserDto.class);

        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.getCurrentUser(user);

        assertNotNull(result);
        assertEquals(dto, result);

        verify(userMapper).toDto(user);
    }

    @Test
    void shouldChangePasswordSuccessfully() {

        User user = new User();
        user.setPasswordHash("encoded-old");

        ChangePasswordRequest request = mock(ChangePasswordRequest.class);
        when(request.oldPassword()).thenReturn("old");
        when(request.newPassword()).thenReturn("new");

        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");

        userService.changePassword(user, request);

        assertEquals("encoded-new", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenOldPasswordInvalid() {

        User user = new User();
        user.setPasswordHash("encoded-old");

        ChangePasswordRequest request = mock(ChangePasswordRequest.class);
        when(request.oldPassword()).thenReturn("wrong");

        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> userService.changePassword(user, request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldUpdateUserSuccessfully() {

        User user = new User();
        user.setId("1");

        UpdateUserRequest request = mock(UpdateUserRequest.class);
        when(request.firstName()).thenReturn("John");
        when(request.lastName()).thenReturn("Doe");

        User saved = new User();
        saved.setFirstName("John");
        saved.setLastName("Doe");

        UserDto dto = mock(UserDto.class);

        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(dto);

        UserDto result = userService.updateUser(user, request);

        assertEquals(dto, result);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());

        verify(userRepository).save(user);
    }

    @Test
    void shouldDeleteUserSuccessfully() {

        User user = new User();
        user.setId("1");
        user.setEmail("test@test.com");
        user.setFirstName("John");

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        userService.deleteUser(user);

        verify(eventPublisher).publishEvent(any(UserDeleteEvent.class));
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundOnDelete() {

        User user = new User();
        user.setId("1");

        when(userRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.deleteUser(user));

        verify(userRepository, never()).delete(any(User.class));
    }
}