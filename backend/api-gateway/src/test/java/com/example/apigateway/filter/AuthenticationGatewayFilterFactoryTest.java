package com.example.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.example.apigateway.error.exception.AuthenticationException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationGatewayFilterFactoryTest {

        private AuthenticationGatewayFilterFactory filterFactory;

        @BeforeEach
        public void setUp() {
                filterFactory = new AuthenticationGatewayFilterFactory();
        }

        @Test
        void authenticationFilter_missingAuthorizationHeader_returns401() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/test").build());

                AuthenticationGatewayFilterFactory.Config config = new AuthenticationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .expectError(AuthenticationException.class)
                                .verify();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        void authenticationFilter_invalidAuthorizationHeader_returns401() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/test")
                                                .header("Authorization", "Bearer invalidToken")
                                                .build());

                AuthenticationGatewayFilterFactory.Config config = new AuthenticationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .expectError(AuthenticationException.class)
                                .verify();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        void authenticationFilter_bearerTokenContainswhitespace_returns401() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/test")
                                                .header("Authorization", "Bearer invalid Token")
                                                .build());

                AuthenticationGatewayFilterFactory.Config config = new AuthenticationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .expectError(AuthenticationException.class)
                                .verify();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        void authenticationFilter_validAuthorizationHeader_successfully() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/test")
                                                .header("Authorization", "Bearer validToken")
                                                .build());

                AuthenticationGatewayFilterFactory.Config config = new AuthenticationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .verifyComplete();
        }
}
