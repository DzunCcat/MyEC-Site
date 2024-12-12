package com.example.apigateway.controller;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apigateway.model.ApiError;

@RestController
public class FallbackController {
    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/fallback/user-service")
    public ResponseEntity<ApiError> userServiceFallback() {
        log.warn("Fallback triggered for user-service");
        return createFallbackResponse(
            "User Service is temporarily unavailable",
            "/api/users"
        );
    }

    @GetMapping("/fallback/product-service")
    public ResponseEntity<ApiError> productServiceFallback() {
        log.warn("Fallback triggered for product-service");
        return createFallbackResponse(
            "Product Service is temporarily unavailable",
            "/api/products"
        );
    }

    @GetMapping("/fallback/order-service")
    public ResponseEntity<ApiError> orderServiceFallback() {
        log.warn("Fallback triggered for order-service");
        return createFallbackResponse(
            "Order Service is temporarily unavailable",
            "/api/orders"
        );
    }

    @GetMapping("/fallback/cart-service")
    public ResponseEntity<ApiError> cartServiceFallback() {
        log.warn("Fallback triggered for cart-service");
        return createFallbackResponse(
            "Cart Service is temporarily unavailable",
            "/api/carts"
        );
    }

    private ResponseEntity<ApiError> createFallbackResponse(String message, String path) {
        ApiError apiError = new ApiError(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Service Unavailable",
            message,
            path,
            Collections.singletonList(
                "Service is not responding. Please try again later."
            )
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiError);
    }
}