package com.example.userservice.error.exception.business;

import org.springframework.http.HttpStatus;

import com.example.userservice.error.exception.base.BaseException;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}