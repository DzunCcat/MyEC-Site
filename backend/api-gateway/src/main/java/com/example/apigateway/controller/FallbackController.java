package com.example.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apigateway.error.response.ApiErrorResponse;

@RestController
public class FallbackController {
    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/fallback/user-service")
    public ResponseEntity<ApiErrorResponse> userServiceFallback() {
        log.warn("Fallback triggered for user-service");
        return createFallbackResponse(
            "User Service is temporarily unavailable",
            "/api/users",
            "user-service"
        );
    }

    @GetMapping("/fallback/product-service")
    public ResponseEntity<ApiErrorResponse> productServiceFallback() {
        log.warn("Fallback triggered for product-service");
        return createFallbackResponse(
            "Product Service is temporarily unavailable",
            "/api/products",
            "product-service"
        );
    }

    @GetMapping("/fallback/order-service")
    public ResponseEntity<ApiErrorResponse> orderServiceFallback() {
        log.warn("Fallback triggered for order-service");
        return createFallbackResponse(
            "Order Service is temporarily unavailable",
            "/api/orders",
            "order-service"
        );
    }

    @GetMapping("/fallback/cart-service")
    public ResponseEntity<ApiErrorResponse> cartServiceFallback() {
        log.warn("Fallback triggered for cart-service");
        return createFallbackResponse(
            "Cart Service is temporarily unavailable",
            "/api/carts",
            "cart-service"
        );
    }

    private ResponseEntity<ApiErrorResponse> createFallbackResponse(
            String message, 
            String path, 
            String serviceName) {
        
        ApiErrorResponse apiError = ApiErrorResponse.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error("Service Unavailable")
            .message(message)
            .path(path)
            .build();

        apiError.addServiceInfo(serviceName, "fallback");
        apiError.getDetails().put("recoveryMessage", 
            "Service is not responding. Please try again later.");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(apiError);
    }
}