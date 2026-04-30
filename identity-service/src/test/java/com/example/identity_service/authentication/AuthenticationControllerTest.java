package com.example.identity_service.authentication;

import com.example.identity_service.authentication.dto.*;
import com.example.identity_service.config.JwtAuthFilter;
import com.example.identity_service.error.GlobalExceptionHandler;
import com.example.identity_service.error.exception.ConflictException;
import com.example.identity_service.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.authcommon.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    JwtService jwtService;
    @MockBean
    UserRepository userRepository;
    @MockBean
    JwtAuthFilter jwtAuthFilter;
    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {

        RegisterRequest request = RegisterRequest.builder()
                .email("test@gmail.com")
                .password("1234")
                .firstName("Joe")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyUsed() throws Exception {

        RegisterRequest request = RegisterRequest.builder()
                .email("test@gmail.com")
                .password("password123")
                .firstName("Joe")
                .lastName("Doe")
                .build();

        doThrow(new ConflictException("Email already exists"))
                .when(authenticationService)
                .register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenRequiredFieldsAreEmpty() throws Exception {

        String requestJson = """
        {
          "email": "",
          "password": "",
          "firstName": "",
          "lastName": ""
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenLoginIsSuccessful() throws Exception {

        AuthenticationRequest request = new AuthenticationRequest(
                "test@gmail.com",
                "password123"
        );

        AuthenticationResponse response = new AuthenticationResponse(
                "access-token",
                "refresh-token",
                null
        );

        org.mockito.Mockito
                .when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

}