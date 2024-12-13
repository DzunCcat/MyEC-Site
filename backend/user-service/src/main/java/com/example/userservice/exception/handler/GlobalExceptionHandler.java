package com.example.userservice.exception.handler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.userservice.exception.base.BaseException;
import com.example.userservice.exception.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        log.error("Handling base exception: {}", ex.getMessage());

        ResponseEntity<ErrorResponse> errorResponse = buildErrorResponse(ex.getStatus(),  ex.getMessage(), request, null);
        
        return errorResponse;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Handling validation exception");

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ResponseEntity<ErrorResponse> errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST,  "入力値の検証に失敗しました", request, details);
        
        return errorResponse;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, WebRequest request) {
        log.error("Handling unexpected exception", ex);
        
        ResponseEntity<ErrorResponse> errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,  "予期せぬエラーが発生しました", request, null);

        return errorResponse;
    }
    
    
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, WebRequest request, List<String> details) {
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(request.getDescription(false).replace("uri=", ""));
        
        if (details != null && !details.isEmpty()) {
            builder.details(details);
        }
        
        return ResponseEntity.status(status).body(builder.build());
    }

    
    
}