package com.example.apigateway.error.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.apigateway.error.contract.ErrorResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse implements ErrorResponse {
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    public void addServiceInfo(String serviceName, String serviceId) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }

        // errorsリストが存在しない場合は作成
        if (!this.details.containsKey("errors")) {
            this.details.put("errors", new ArrayList<String>());
        }

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) this.details.get("errors");
        errors.add("serviceName: " + serviceName);
        errors.add("serviceId: " + serviceId);
    }

    // エラーメッセージ追加用のメソッド
    public void addErrorMessage(String errorMessage) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }

        if (!this.details.containsKey("errors")) {
            this.details.put("errors", new ArrayList<String>());
        }

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) this.details.get("errors");
        errors.add(errorMessage);
    }
}