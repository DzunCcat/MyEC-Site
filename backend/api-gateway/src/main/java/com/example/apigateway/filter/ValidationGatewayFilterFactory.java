package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.example.apigateway.error.exception.InvalidRequestException;

import reactor.core.publisher.Mono;

@Component
public class ValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ValidationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(ValidationGatewayFilterFactory.class);

    public ValidationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!isValidContentType(request)) {
                log.error("Invalid content type");
                return onError(exchange, "Invalid content type", HttpStatus.BAD_REQUEST);
            }

            if (!isValidRequestSize(exchange)) {
                log.error("Request size exceeds limit");
                return onError(exchange, "Request size exceeds limit", HttpStatus.PAYLOAD_TOO_LARGE);
            }

            return chain.filter(exchange);
        };
    }

    private boolean isValidContentType(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        MediaType contentType = headers.getContentType();

        if (request.getMethod() == HttpMethod.GET) {
            return true;
        }

        return contentType != null &&
                (MediaType.APPLICATION_JSON.isCompatibleWith(contentType) ||
                        MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType));
    }

    private boolean isValidRequestSize(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        long contentLength = headers.getContentLength();
        long maxSize = 10 * 1024 * 1024; // 10MB
        return contentLength <= maxSize;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        exchange.getResponse().setStatusCode(httpStatus);

        return Mono.error(new InvalidRequestException(err));
    }

    public static class Config {
        // 必要に応じて設定プロパティを追加
    }
}