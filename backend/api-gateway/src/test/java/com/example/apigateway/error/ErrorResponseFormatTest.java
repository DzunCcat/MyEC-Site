package com.example.apigateway.error;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ErrorResponseFormatTest {

        @Autowired
        private WebTestClient webTestClient;

        @Test
        void errorResponseShouldFollowCommonFormat() {
                // Test authentication error (401)
                webTestClient.get().uri("/api/users/profile")
                                .exchange()
                                .expectStatus().isUnauthorized()
                                .expectBody()
                                .jsonPath("$.timestamp").exists()
                                .jsonPath("$.status").isEqualTo(401)
                                .jsonPath("$.error").isEqualTo("Unauthorized")
                                .jsonPath("$.message").isEqualTo("Authorization header is required")
                                .jsonPath("$.path").exists()
                                .jsonPath("$.details").exists()
                                .jsonPath("$.details.errors").isArray()
                                .jsonPath("$.details.errors[0]").isEqualTo("Authorization header is required");

                // Test validation error (400) - Content type validation
                webTestClient.post().uri("/api/users")
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue("invalid content type")
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.timestamp").exists()
                                .jsonPath("$.status").isEqualTo(400)
                                .jsonPath("$.error").isEqualTo("Bad Request")
                                .jsonPath("$.message").isEqualTo("Invalid content type")
                                .jsonPath("$.path").exists()
                                .jsonPath("$.details").exists()
                                .jsonPath("$.details.errors").isArray()
                                .jsonPath("$.details.errors[0]").isEqualTo("Invalid content type");

                // Test validation error (400) - Request body validation
                webTestClient.post().uri("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"username\":\"\",\"email\":\"invalid\"}")
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.timestamp").exists()
                                .jsonPath("$.status").isEqualTo(400)
                                .jsonPath("$.error").isEqualTo("Bad Request")
                                .jsonPath("$.message").exists()
                                .jsonPath("$.path").exists()
                                .jsonPath("$.details").exists()
                                .jsonPath("$.details.errors").isArray();

                // Test service unavailable error (503)
                webTestClient.get().uri("/fallback/user-service")
                                .exchange()
                                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                                .expectBody()
                                .jsonPath("$.timestamp").exists()
                                .jsonPath("$.status").isEqualTo(503)
                                .jsonPath("$.error").isEqualTo("Service Unavailable")
                                .jsonPath("$.message").isEqualTo("User Service is temporarily unavailable")
                                .jsonPath("$.path").isEqualTo("/api/users")
                                .jsonPath("$.details").exists()
                                .jsonPath("$.details.errors").isArray()
                                .jsonPath("$.details.errors[0]").isEqualTo("User Service is temporarily unavailable")
                                .jsonPath("$.details.errors[1]")
                                .isEqualTo("Service is not responding. Please try again later.")
                                .jsonPath("$.details.errors[2]").isEqualTo("serviceName: user-service")
                                .jsonPath("$.details.errors[3]").isEqualTo("serviceId: fallback");
        }

        @Test
        void errorResponseShouldHandleInvalidRequest() {
                // Test request size limit exceeded (413)
                webTestClient.post().uri("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Content-Length", String.valueOf(11 * 1024 * 1024)) // 11MB (exceeds 10MB limit)
                                .bodyValue("{}")
                                .exchange()
                                .expectStatus().isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
                                .expectBody()
                                .jsonPath("$.timestamp").exists()
                                .jsonPath("$.status").isEqualTo(413)
                                .jsonPath("$.error").isEqualTo("Payload Too Large")
                                .jsonPath("$.message").isEqualTo("Request size exceeds limit")
                                .jsonPath("$.path").exists()
                                .jsonPath("$.details").exists()
                                .jsonPath("$.details.errors").isArray()
                                .jsonPath("$.details.errors[0]").isEqualTo("Request size exceeds limit");
        }
}