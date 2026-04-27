package com.example.identity_service.user;


import com.example.identity_service.TestData;
import com.example.identity_service.authentication.JwtService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserIT {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    JwtService jwtService;
    @Autowired
    TestData testData;

    private User user;
    private String token;

    @BeforeEach
    public void setUp() {
        user = userRepository.save(testData.userBuilder().build());
        token = "Bearer " + jwtService.generateToken(user);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()));
    }

    @Test
    void shouldChangePassword() throws Exception {

        mockMvc.perform(post("/api/users/change-password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "oldPassword": "password123",
                    "newPassword": "password1234"
                }
            """))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateUser() throws Exception {

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "firstName": "Jane",
                    "lastName": "Doe"
                }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailWhenOldPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/api/users/change-password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "oldPassword": "wrongPassword",
                            "newPassword": "password1234"
                        }
                    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {

        String invalidToken = "Bearer " + token + "corrupted";

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized());
    }
}