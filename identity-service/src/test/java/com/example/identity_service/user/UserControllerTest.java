package com.example.identity_service.user;

import com.example.identity_service.user.dto.ChangePasswordRequest;
import com.example.identity_service.user.dto.UpdateUserRequest;
import com.example.identity_service.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnCurrentUser() {

        User user = new User();
        UserDto dto = mock(UserDto.class);

        when(authentication.getPrincipal()).thenReturn(user);
        when(userService.getCurrentUser(user)).thenReturn(dto);

        ResponseEntity<UserDto> response = userController.me(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldChangePassword() {

        User user = new User();
        ChangePasswordRequest request = mock(ChangePasswordRequest.class);

        when(authentication.getPrincipal()).thenReturn(user);

        ResponseEntity<Void> response =
                userController.changePassword(authentication, request);

        assertEquals(204, response.getStatusCode().value());

        verify(userService).changePassword(user, request);
    }

    @Test
    void shouldUpdateUserData() {

        User user = new User();
        UpdateUserRequest request = mock(UpdateUserRequest.class);
        UserDto dto = mock(UserDto.class);

        when(authentication.getPrincipal()).thenReturn(user);
        when(userService.updateUser(user, request)).thenReturn(dto);

        ResponseEntity<UserDto> response =
                userController.updateUserData(authentication, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldDeleteUser() {

        User user = new User();

        when(authentication.getPrincipal()).thenReturn(user);

        ResponseEntity<Void> response =
                userController.deleteUser(authentication);

        assertEquals(204, response.getStatusCode().value());

        verify(userService).deleteUser(user);
    }
}