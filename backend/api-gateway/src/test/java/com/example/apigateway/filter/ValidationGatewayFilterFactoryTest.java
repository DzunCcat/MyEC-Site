package com.example.apigateway.filter;

import static org.springframework.http.MediaType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.example.apigateway.error.exception.InvalidRequestException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ValidationGatewayFilterFactoryTest {
        private ValidationGatewayFilterFactory filterFactory;

        @BeforeEach
        public void setUp() {
                filterFactory = new ValidationGatewayFilterFactory();
        }

        @Test
        void validationFilter_jsonContentType_successfully() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.post("/test")
                                                .contentType(APPLICATION_JSON)
                                                .header("Content-Length", "100")
                                                .build());

                ValidationGatewayFilterFactory.Config config = new ValidationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .verifyComplete();
        }

        @Test
        void validationFilter_formUrlencodedContentType_successfully() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.post("/test")
                                                .contentType(APPLICATION_FORM_URLENCODED)
                                                .header("Content-Length", "100")
                                                .build());

                ValidationGatewayFilterFactory.Config config = new ValidationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .verifyComplete();
        }

        @Test
        void validationFilter_invalidContentType_return400() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.post("/test")
                                                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                                                .header("Content-Length", "100")
                                                .build());

                ValidationGatewayFilterFactory.Config config = new ValidationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .expectError(InvalidRequestException.class)
                                .verify();

                assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        }

        @Test
        void validationFilter_getRequest_skipsContentTypeCheck_successfully() {
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/test").build());

                ValidationGatewayFilterFactory.Config config = new ValidationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .verifyComplete();
        }

        @Test
        void validationFilter_requestSizeExceedsLimit_return413() {
                long elevenMB = 11 * 1024 * 1024;
                ServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.post("/test")
                                                .contentType(APPLICATION_JSON)
                                                .header("Content-Length", String.valueOf(elevenMB))
                                                .build());

                ValidationGatewayFilterFactory.Config config = new ValidationGatewayFilterFactory.Config();
                Mono<Void> result = filterFactory.apply(config).filter(exchange, e -> Mono.empty());

                StepVerifier.create(result)
                                .expectSubscription()
                                .expectError(InvalidRequestException.class)
                                .verify();

                assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, exchange.getResponse().getStatusCode());
        }
}
