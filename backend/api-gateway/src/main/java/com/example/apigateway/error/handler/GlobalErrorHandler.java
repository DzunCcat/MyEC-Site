package com.example.apigateway.error.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.apigateway.error.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Global error handler caught exception", ex);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiErrorResponse apiError = ApiErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(ex.getMessage())
            .path(exchange.getRequest().getPath().toString())
            .build();

        if (ex.getMessage() != null) {
            apiError.addServiceInfo("system", "error-handler");
            apiError.getDetails().put("errorDetail", ex.getMessage());
        }

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(apiError);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing error response", e);
            return Mono.error(e);
        }
    }
}