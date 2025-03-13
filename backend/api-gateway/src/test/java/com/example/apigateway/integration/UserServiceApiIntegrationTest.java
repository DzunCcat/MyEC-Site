package com.example.apigateway.integration;

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

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class UserServiceApiIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockBean
        private FallbackController fallbackController;

        @BeforeEach
        void setUp() {
                // フォールバックコントローラーの設定
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
        void shouldValidateUserCreationRequest() {

                // 意図的に3つの無効な値を持つJSONリクエストを作成:
                // 空のユーザー名
                // 不正なメールアドレス形式
                // 最小長（8文字）を満たさないパスワード

                String invalidRequest = "{\"username\":\"\",\"email\":\"invalid-email\",\"password\":\"short\"}";

                webTestClient.post().uri("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(invalidRequest)
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(400)
                                .jsonPath("$.error").isEqualTo("Bad Request")
                                .jsonPath("$.message").exists()
                                .jsonPath("$.details.errors").isArray()
                                .jsonPath("$.details.errors[?(@=='Usernameは必須です。')]").exists()
                                .jsonPath("$.details.errors[?(@=='有効なEmailを入力してください。')]").exists()
                                .jsonPath("$.details.errors[?(@=='Passwordを8文字以上で入力してください。')]").exists();
        }

        @Test
        void shouldReturnUnauthorizedWhenNoAuthHeaderProvided() {
                String anyUserId = "123e4567-e89b-12d3-a456-426614174000";

                webTestClient.get().uri("/api/users/" + anyUserId)
                                .exchange()
                                .expectStatus().isUnauthorized()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(401)
                                .jsonPath("$.error").isEqualTo("Unauthorized")
                                .jsonPath("$.message").isEqualTo("Authorization header is required");
        }

        @Test
        void checkActualErrorResponseStructure() {
                String nonExistentUserId = "123e4567-e89b-12d3-a456-426614174000";

                String responseBody = webTestClient.get().uri("/api/users/" + nonExistentUserId)
                                .header("Authorization", "Bearer validToken")
                                .exchange()
                                .expectStatus().isNotFound()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();

                System.out.println("Actual error response: " + responseBody);
        }

        @Test
        void shouldHandleInvalidUuidFormat() {
                // 無効なUUID形式のリクエスト
                String invalidUuid = "invalid-uuid";

                webTestClient.get().uri("/api/users/" + invalidUuid)
                                .header("Authorization", "Bearer validToken")
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(400)
                                .jsonPath("$.error").isEqualTo("Bad Request")
                                .jsonPath("$.message").isEqualTo("Invalid UUID format: " + invalidUuid)
                                .jsonPath("$.details.errors[?(@=='Invalid UUID format: " + invalidUuid + "')]").exists()
                                .jsonPath("$.details.errors[?(@=='不正なUUID: " + invalidUuid + "')]").exists();
        }

        @Test
        void shouldHandleCircuitBreakerFallback() {
                // サーキットブレーカー発動時のフォールバック応答を確認
                webTestClient.get().uri("/fallback/user-service")
                                .exchange()
                                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                                .expectBody()
                                .jsonPath("$.status").isEqualTo(503)
                                .jsonPath("$.error").isEqualTo("Service Unavailable")
                                .jsonPath("$.message").isEqualTo("User Service is temporarily unavailable")
                                .jsonPath("$.details.serviceName").isEqualTo("user-service")
                                .jsonPath("$.details.serviceId").isEqualTo("fallback");
        }
}