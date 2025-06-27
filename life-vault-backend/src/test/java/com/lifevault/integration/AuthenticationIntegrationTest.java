package com.lifevault.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifevault.dto.LoginRequest;
import com.lifevault.dto.SignupRequest;
import com.lifevault.entity.User;
import com.lifevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void signup_Success() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");
        request.setFirstName("New");
        request.setLastName("User");

        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"))
                .andReturn();

        // Verify user was created in database
        User createdUser = userRepository.findByEmail("newuser@example.com").orElse(null);
        assertNotNull(createdUser);
        assertEquals("New", createdUser.getFirstName());
        assertEquals("User", createdUser.getLastName());
        assertEquals(180, createdUser.getInactivityPeriodDays()); // Default value
    }

    @Test
    void signup_DuplicateEmail() throws Exception {
        // Create existing user
        SignupRequest firstRequest = new SignupRequest();
        firstRequest.setEmail("existing@example.com");
        firstRequest.setPassword("Password123!");
        firstRequest.setFirstName("Existing");
        firstRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Try to create duplicate
        SignupRequest duplicateRequest = new SignupRequest();
        duplicateRequest.setEmail("existing@example.com");
        duplicateRequest.setPassword("DifferentPassword123!");
        duplicateRequest.setFirstName("Duplicate");
        duplicateRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Email is already in use!"));
    }

    @Test
    void signup_InvalidEmail() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setEmail("invalid-email");
        request.setPassword("Password123!");
        request.setFirstName("Invalid");
        request.setLastName("Email");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        // First create a user
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("testlogin@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("Login");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Now try to login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testlogin@example.com");
        loginRequest.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testlogin@example.com"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Verify last activity was updated
        User loggedInUser = userRepository.findByEmail("testlogin@example.com").orElse(null);
        assertNotNull(loggedInUser);
        assertNotNull(loggedInUser.getLastActivityAt());
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        // First create a user
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("validuser@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setFirstName("Valid");
        signupRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("validuser@example.com");
        loginRequest.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_NonExistentUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpoint_WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_WithValidToken() throws Exception {
        // Create user and get token
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("protected@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setFirstName("Protected");
        signupRequest.setLastName("User");

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // First login to get the token since signup doesn't return it
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("protected@example.com");
        loginReq.setPassword("Password123!");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // Access protected endpoint with token
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("protected@example.com"));
    }

    @Test
    void cors_PreflightRequest() throws Exception {
        mockMvc.perform(options("/api/auth/signup")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}