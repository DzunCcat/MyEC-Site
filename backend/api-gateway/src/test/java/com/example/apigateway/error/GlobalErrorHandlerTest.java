package com.example.apigateway.error;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.example.apigateway.error.exception.AuthenticationException;
import com.example.apigateway.error.handler.GlobalErrorHandler;

import reactor.core.publisher.Mono;

@WebFluxTest
@Import(GlobalErrorHandler.class)
@ActiveProfiles("test")
public class GlobalErrorHandlerTest {

    @Autowired
    private GlobalErrorHandler globalErrorHandler;

    @MockBean
    private WebTestClient webTestClient;

    @Test
    void shouldHandleResponseStatusException() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest().getPath().toString()).thenReturn("/api/test");

        AuthenticationException ex = new AuthenticationException("Authentication failed");

        Mono<Void> result = globalErrorHandler.handle(exchange, ex);

    }
}