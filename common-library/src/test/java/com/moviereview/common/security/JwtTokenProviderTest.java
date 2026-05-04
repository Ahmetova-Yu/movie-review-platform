package com.moviereview.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "myTestSecretKeyForJWTTokenGeneration2024VeryLong");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // given
        String username = "anna";
        Long userId = 1L;
        String role = "USER";

        // when
        String token = jwtTokenProvider.generateToken(username, userId, role);

        // then
        assertNotNull(token);
        assertTrue(token.length() > 50);
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        // given
        String token = jwtTokenProvider.generateToken("anna", 1L, "USER");

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertTrue(isValid);
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertFalse(isValid);
    }

    @Test
    void validateToken_withNullToken_shouldReturnFalse() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(null);

        // then
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        // given
        String expectedUsername = "boris";
        String token = jwtTokenProvider.generateToken(expectedUsername, 2L, "USER");

        // when
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        // given
        Long expectedUserId = 42L;
        String token = jwtTokenProvider.generateToken("user", expectedUserId, "USER");

        // when
        Long actualUserId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        // given
        String expectedRole = "ADMIN";
        String token = jwtTokenProvider.generateToken("admin", 1L, expectedRole);

        // when
        String actualRole = jwtTokenProvider.getRoleFromToken(token);

        // then
        assertEquals(expectedRole, actualRole);
    }
}