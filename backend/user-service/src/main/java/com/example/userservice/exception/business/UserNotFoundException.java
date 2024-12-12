package com.example.userservice.exception.business;

import org.springframework.http.HttpStatus;

import com.example.userservice.exception.base.BaseException;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}