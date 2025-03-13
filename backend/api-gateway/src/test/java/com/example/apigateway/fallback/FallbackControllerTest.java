package com.example.apigateway.fallback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.example.apigateway.controller.FallbackController;
import com.example.apigateway.error.response.ApiErrorResponse;

@SpringBootTest
@ActiveProfiles("test")
public class FallbackControllerTest {

    @Autowired
    private FallbackController fallbackController;

    @Test
    void userServiceFallbackShouldReturnProperErrorResponse() {
        ResponseEntity<ApiErrorResponse> response = fallbackController.userServiceFallback();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(503);
        assertThat(errorResponse.getError()).isEqualTo("Service Unavailable");
        assertThat(errorResponse.getMessage()).contains("User Service is temporarily unavailable");
        assertThat(errorResponse.getPath()).isEqualTo("/api/users");
        assertThat(errorResponse.getDetails()).containsKey("serviceName");
        assertThat(errorResponse.getDetails().get("serviceName")).isEqualTo("user-service");
    }
}