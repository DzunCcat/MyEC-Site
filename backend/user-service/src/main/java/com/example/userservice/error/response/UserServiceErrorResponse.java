package com.example.userservice.error.response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.example.userservice.error.contract.ErrorResponse;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserServiceErrorResponse implements ErrorResponse {
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    public static class UserServiceErrorResponseBuilder {
       // カスタム用
    }
}
