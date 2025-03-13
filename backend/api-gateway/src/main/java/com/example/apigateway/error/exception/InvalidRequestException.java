package com.example.apigateway.error.exception;

import org.springframework.http.HttpStatus;

import com.example.apigateway.error.exception.base.BaseException;

public class InvalidRequestException extends BaseException {
    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}