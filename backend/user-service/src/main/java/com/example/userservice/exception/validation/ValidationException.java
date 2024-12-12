package com.example.userservice.exception.validation;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.example.userservice.exception.base.BaseException;

public class ValidationException extends BaseException {
    private final List<String> details;
    
    public ValidationException(String message) {
    	super(message, HttpStatus.BAD_REQUEST);
        this.details = null;
    }

    public ValidationException(String message, List<String> details) {
        super(message, HttpStatus.BAD_REQUEST);
        this.details = details;
    }

    public List<String> getDetails() {
        return details;
    }
}