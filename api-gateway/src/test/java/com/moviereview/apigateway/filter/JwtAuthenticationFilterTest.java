package com.moviereview.apigateway.filter;

import com.moviereview.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class JwtAuthenticationFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        validToken = "valid.jwt.token";
        invalidToken = "invalid.token";

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromToken(validToken)).thenReturn("USER");
    }

    @Test
    void requestWithoutToken_shouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/api/films")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void requestWithValidToken_shouldSucceed() {
        webTestClient.get()
                .uri("/api/films")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void requestWithInvalidToken_shouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/api/films")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authEndpoint_withoutToken_shouldSucceed() {
        webTestClient.post()
                .uri("/api/auth/login")
                .exchange()
                .expectStatus().isBadRequest(); // тело пустое, но не 401
    }
}