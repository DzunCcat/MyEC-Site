package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.example.apigateway.error.exception.AuthenticationException;
import com.example.apigateway.error.exception.InvalidRequestException;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationGatewayFilterFactory.class);

    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Authorization header is missing or invalid");
                return onError(exchange, "Authorization header is required", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            if (!isValidToken(token)) {
                log.error("Invalid token provided");
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        exchange.getResponse().setStatusCode(httpStatus);

        if (httpStatus == HttpStatus.UNAUTHORIZED) {
            return Mono.error(new AuthenticationException(err));
        } else {
            return Mono.error(new InvalidRequestException(err));
        }
    }

    private boolean isValidToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.debug("Token is null or empty");
                return false;
            }
            if (token.contains(" ")) {
                log.debug("Token contains whitespace");
                return false;
            }
            return "validToken".equals(token);
        } catch (Exception e) {
            log.error("Basic token validation failed", e);
            return false;
        }
    }

    public static class Config {
        // 必要に応じて設定プロパティを追加
    }
}
