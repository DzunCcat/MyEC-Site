package com.example.apigateway.error.response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.example.apigateway.error.contract.ErrorResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse implements ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private Map<String, Object> details;

    public static class ApiErrorResponseBuilder {
        private Map<String, Object> details = new HashMap<>();
        private LocalDateTime timestamp = LocalDateTime.now();
    }


    // サービス固有のメソッド
    public void addServiceInfo(String serviceName, String serviceId) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put("serviceName", serviceName);
        this.details.put("serviceId", serviceId);
    }
}