package com.example.identity_service.authentication;

import com.example.identity_service.TestData;
import com.example.identity_service.user.User;
import com.example.identity_service.user.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.time.LocalDateTime;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthenticationIT {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TestData testData;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    void shouldReturnRegisteredUser() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                  "email": "test@gmail.com",
                  "password": "password1234",
                  "firstName": "Test",
                  "lastName": "Test"
            }
        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.user.firstName").value("Test"))
                .andExpect(jsonPath("$.user.lastName").value("Test"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"));

        User createdUser = userRepository.findByEmail("test@gmail.com")
                .orElseThrow(() -> new AssertionFailure("User was not created"));

        assertNotNull(createdUser.getId());
        assertEquals("test@gmail.com", createdUser.getEmail());

        Token token = tokenRepository.findByUser(createdUser).orElseThrow();
        assertNotNull(token.getRefreshToken());

    }

    @Test
    void shouldAuthenticateUser() throws Exception {

        User user = testData.userBuilder().build();
        userRepository.save(user);

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                  "email": "email@test.pl",
                  "password": "password123"
            }
        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.email").value("email@test.pl"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.lastName").value("Doe"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"));

        Token token = tokenRepository.findByUser(user).orElseThrow();
        assertNotNull(token.getRefreshToken());
    }

    @Test
    void shouldRefreshToken() throws Exception {

        User user = testData.userBuilder().build();
        userRepository.save(user);

        Token token = testData.tokenBuilder().user(user).build();
        tokenRepository.save(token);

        String oldRefreshToken = token.getRefreshToken();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                          "token": "%s"
                                    }
                                """.formatted(oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.email").value("email@test.pl"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.lastName").value("Doe"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"));

        Token newToken = tokenRepository.findByUser(user).orElseThrow();

        assertNotNull(newToken.getRefreshToken());
        assertNotEquals(oldRefreshToken, newToken.getRefreshToken());

    }

    @Test
    void shouldLogoutUser() throws Exception {

        User user = userRepository.save(testData.userBuilder().build());

        String jwt = jwtService.generateToken(user);

        Token token = testData.tokenBuilder()
                .user(user)
                .build();
        tokenRepository.save(token);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());

        assertTrue(tokenRepository.findByUser(user).isEmpty());
    }

    @Nested
    public class passwordResetSuccess {

        @Test
        void shouldRequestPasswordReset() throws Exception {

            User user = testData.userBuilder().build();
            userRepository.save(user);

            mockMvc.perform(post("/api/auth/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
        {
              "email": "email@test.pl"
        }
    """))
                    .andExpect(status().isNoContent());

            PasswordResetToken token =
                    passwordResetTokenRepository.findByUser(user).orElseThrow();

            assertNotNull(token.getToken());
        }

        @Test
        void shouldResetPassword() throws Exception {

            User user = userRepository.save(testData.userBuilder().build());

            mockMvc.perform(post("/api/auth/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                {
                      "email": "email@test.pl"
                }
            """))
                    .andExpect(status().isNoContent());

            PasswordResetToken resetToken =
                    passwordResetTokenRepository.findByUser(user).orElseThrow();

            String newPassword = "password1234";

            String tokenValue = resetToken.getToken();

            mockMvc.perform(post("/api/auth/password-reset/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                            {
                                                  "token": "%s",
                                                  "newPassword": "%s"
                                            }
                                    """.formatted(tokenValue, newPassword)))
                    .andExpect(status().isNoContent());

            assertFalse(passwordResetTokenRepository.existsById(resetToken.getId()));

            User updatedUser = userRepository.findById(user.getId()).orElseThrow();

            assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPasswordHash()));
        }
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
        {
              "token": "invalid-token"
        }
    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsExpired() throws Exception {

        User user = userRepository.save(testData.userBuilder().build());

        Token token = Token.builder()
                .user(user)
                .refreshToken("expired-token")
                .createdAt(java.time.LocalDateTime.now().minusDays(10))
                .expiresAt(java.time.LocalDateTime.now().minusDays(1)) // EXPIRED
                .revoked(false)
                .build();

        tokenRepository.save(token);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
        {
              "token": "expired-token"
        }
    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
        {
              "email": "wrong@email.com",
              "password": "wrongpass"
        }
    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundWhenResetTokenDoesNotExist() throws Exception {

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
        {
              "token": "fake-token",
              "newPassword": "newPassword123"
        }
    """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteResetTokenAfterSuccessfulReset() throws Exception {

        User user = userRepository.save(testData.userBuilder().build());

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        passwordResetTokenRepository.saveAndFlush(token);

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
        {
          "token": "valid-token",
          "newPassword": "newPassword123"
        }
    """))
                .andExpect(status().isNoContent());

        assertTrue(passwordResetTokenRepository.findByToken("valid-token").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenResetTokenIsUsedSecondTime() throws Exception {

        User user = userRepository.save(testData.userBuilder().build());

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        passwordResetTokenRepository.saveAndFlush(token);

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
        {
          "token": "valid-token",
          "newPassword": "newPassword123"
        }
    """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
        {
          "token": "valid-token",
          "newPassword": "newPassword123"
        }
    """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        User user = testData.userBuilder().build();
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                          "email": "%s",
                                          "password": "password1234",
                                          "firstName": "John",
                                          "lastName": "Doe"
                                    }
                                """.formatted(user.getEmail())))
                .andExpect(status().isConflict());
    }

}