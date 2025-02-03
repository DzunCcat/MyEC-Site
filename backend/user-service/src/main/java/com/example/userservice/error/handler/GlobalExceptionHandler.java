package com.example.userservice.error.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.example.userservice.error.exception.base.BaseException;
import com.example.userservice.error.exception.business.UserAlreadyExistsException;
import com.example.userservice.error.exception.business.UserNotFoundException;
import com.example.userservice.error.exception.validation.ValidationException;
import com.example.userservice.error.response.UserServiceErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //BaseException(ビジネス例外)の共通ハンドラ - UserNotFoundException, UserAlreadyExistsExceptionなど
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<UserServiceErrorResponse> handleBaseException(
            BaseException ex,
            WebRequest request
    ) {
        log.error("Handling base exception: {}", ex.getMessage());

        UserServiceErrorResponse errorResponse = UserServiceErrorResponse.builder()
            .status(ex.getStatus().value())
            .error(ex.getStatus().getReasonPhrase())
            .message(ex.getMessage())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();

        
        Map<String, Object> detailsMap = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        errors.add(ex.getMessage());

        if (ex instanceof UserNotFoundException) {
            String userId = ex.getMessage().substring(ex.getMessage().lastIndexOf(" ") + 1);
            errors.add("ユーザID: " + userId);
        } else if (ex instanceof UserAlreadyExistsException) {
            String identifier = ex.getMessage().split(" ")[1];
            errors.add("重複ユーザ: " + identifier);
        }

        detailsMap.put("errors", errors);
        errorResponse.setDetails(detailsMap);

        return ResponseEntity
            .status(ex.getStatus())
            .body(errorResponse);
    }

     //Springのバリデーション(@Valid)で引っかかった場合のハンドラ - MethodArgumentNotValidException

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UserServiceErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.error("Handling validation exception: {}", ex.getMessage());

        List<String> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());

        UserServiceErrorResponse errorResponse = UserServiceErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("入力値の検証に失敗しました")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();

        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("errors", validationErrors);
        errorResponse.setDetails(detailsMap);

        return ResponseEntity
            .badRequest()
            .body(errorResponse);
    }

	//アプリ内で独自定義している ValidationException (Invalid UUIDなど)
	
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<UserServiceErrorResponse> handleCustomValidationException(
            ValidationException ex,
            WebRequest request
    ) {
        log.error("Handling custom ValidationException: {}", ex.getMessage());
        
        UserServiceErrorResponse errorResponse = UserServiceErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(ex.getMessage()) // 例えば "Invalid UUID format: ...."
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();

        Map<String, Object> detailsMap = new HashMap<>();
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());

        if (ex.getMessage().startsWith("Invalid UUID format:")) {
            String invalidValue = ex.getMessage().replace("Invalid UUID format: ", "");
            errors.add("不正なUUID: " + invalidValue);
        }

        detailsMap.put("errors", errors);
        errorResponse.setDetails(detailsMap);

        return ResponseEntity.badRequest().body(errorResponse);
    }

     //予期しない例外 (RuntimeException, それ以外のException)
     
    @ExceptionHandler(Exception.class)
    public ResponseEntity<UserServiceErrorResponse> handleUnexpectedException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Handling unexpected exception", ex);

        UserServiceErrorResponse errorResponse = UserServiceErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("予期せぬエラーが発生しました")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();

        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("errors", List.of("ErrorType: " + ex.getClass().getSimpleName()));
        errorResponse.setDetails(detailsMap);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
}