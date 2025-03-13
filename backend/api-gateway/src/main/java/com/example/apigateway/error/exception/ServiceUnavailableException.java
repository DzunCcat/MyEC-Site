package com.example.apigateway.error.exception;

import org.springframework.http.HttpStatus;

import com.example.apigateway.error.exception.base.BaseException;

public class ServiceUnavailableException extends BaseException {
    private final String serviceName;

    public ServiceUnavailableException(String message, String serviceName) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}