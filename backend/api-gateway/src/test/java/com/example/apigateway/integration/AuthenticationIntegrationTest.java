package com.example.apigateway.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.apigateway.filter.AuthenticationGatewayFilterFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockBean
        private AuthenticationGatewayFilterFactory authenticationFilter;

        @Test
        void shouldHandleJwtAuthenticationFlow() {
                webTestClient.get().uri("/api/users/me")
                                .header("Authorization", "Invalid-Format")
                                .exchange()
                                .expectStatus().isUnauthorized()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(401)
                                .jsonPath("$.message").isEqualTo("Authorization header is required");

                webTestClient.get().uri("/api/users/me")
                                .header("Authorization", "Bearer invalidToken")
                                .exchange()
                                .expectStatus().isUnauthorized()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(401)
                                .jsonPath("$.message").isEqualTo("Invalid token");

                webTestClient.delete().uri("/api/users/admin/123")
                                .header("Authorization", "Bearer validToken")
                                .exchange()
                                .expectStatus().isForbidden()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(403)
                                .jsonPath("$.message").isEqualTo("Access Denied")
                                .jsonPath("$.details.errors[?(@=='アクセスが拒否されました')]").exists();
        }

        @Test
        void shouldValidateRequestBody() {
                webTestClient.post().uri("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{}")
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(400)
                                .jsonPath("$.details.errors").isArray()
                                .jsonPath("$.details.errors.length()").isEqualTo(3); // username, email, password
        }
}