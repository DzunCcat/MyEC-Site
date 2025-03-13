package com.example.apigateway.integration;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.apigateway.controller.FallbackController;
import com.example.apigateway.error.response.ApiErrorResponse;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ApiGatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FallbackController fallbackController;

    @BeforeEach
    void setUp() {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message("User Service is temporarily unavailable")
                .path("/api/users")
                .build();

        errorResponse.addServiceInfo("user-service", "fallback");

        when(fallbackController.userServiceFallback())
                .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(errorResponse));
    }

    @Test
    void shouldRequireAuthenticationForUserService() {
        webTestClient.get().uri("/api/users/profile")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.error").isEqualTo("Unauthorized")
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldValidateContentTypeForPostRequests() {
        webTestClient.post().uri("/api/users")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("invalid content type")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldValidateRequestBodyForUserCreation() {
        webTestClient.post().uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"\",\"email\":\"invalid\",\"password\":\"short\"}")
                .header("Authorization", "Bearer validToken")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").exists()
                .jsonPath("$.details.errors").isArray();
    }

    @Test
    void shouldActivateFallbackWhenUserServiceUnavailable() {
        webTestClient.get().uri("/api/users/profile")
                .header("Authorization", "Bearer validToken")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable")
                .jsonPath("$.message").isEqualTo("User Service is temporarily unavailable")
                .jsonPath("$.details.serviceName").isEqualTo("user-service");
    }
}