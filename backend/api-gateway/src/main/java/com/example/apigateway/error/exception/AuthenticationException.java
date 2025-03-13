package com.example.apigateway.error.exception;

import org.springframework.http.HttpStatus;

import com.example.apigateway.error.exception.base.BaseException;

public class AuthenticationException extends BaseException {
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}