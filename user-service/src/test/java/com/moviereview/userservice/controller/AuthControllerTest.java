package com.moviereview.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviereview.common.dto.AuthRequestDTO;
import com.moviereview.common.dto.AuthResponseDTO;
import com.moviereview.common.security.JwtTokenProvider;
import com.moviereview.userservice.dto.RegisterRequest;
import com.moviereview.userservice.entity.User;
import com.moviereview.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_withValidData_shouldReturnToken() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@mail.com");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void register_withExistingUsername_shouldReturnError() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("existing@mail.com");
        request.setPassword("password123");

        userRepository.save(User.builder()
                .username("existinguser")
                .email("existing@mail.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role("USER")
                .build());

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        // given
        userRepository.save(User.builder()
                .username("loginuser")
                .email("login@mail.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .role("USER")
                .build());

        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("loginuser");
        request.setPassword("secret123");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_withInvalidPassword_shouldReturnError() throws Exception {
        // given
        userRepository.save(User.builder()
                .username("loginuser2")
                .email("login2@mail.com")
                .passwordHash(passwordEncoder.encode("correct123"))
                .role("USER")
                .build());

        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("loginuser2");
        request.setPassword("wrong");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}