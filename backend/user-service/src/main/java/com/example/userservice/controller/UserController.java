package com.example.userservice.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.validation.ValidationException;
import com.example.userservice.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Received request to create user with username: {}", request.getUsername());
        
        try {
            UserResponse createdUser = userService.createUser(request);
            log.info("Successfully created user with ID: {}", createdUser.getId());
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);

        } catch (Exception e) {
            log.error("Error occurred while creating user", e);
            throw e; 
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.info("Received request to get user with ID: {}", id);
        
        try {
            UUID uuid = parseUUID(id);
            UserResponse user = userService.getUserById(uuid);
            log.info("Successfully retrieved user with ID: {}", id);
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error occurred while fetching user with ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(authentication, #id)") 
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id, 
            @Valid @RequestBody CreateUserRequest request) {
        log.info("Received request to update user with ID: {}", id);
        
        try {
            UUID uuid = parseUUID(id);
            UserResponse updatedUser = userService.updateUser(uuid, request);
            log.info("Successfully updated user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error occurred while updating user with ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(authentication, #id)") 
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("Received request to delete user with ID: {}", id);
        
        try {
            UUID uuid = parseUUID(id);
            userService.deleteUser(uuid);
            log.info("Successfully deleted user with ID: {}", id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error occurred while deleting user with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * 文字列をUUIDに変換する。
     * 無効なUUID形式の場合は適切な例外をスローする。
     * 
     * @param id UUID形式の文字列
     * @return 変換されたUUID
     * @throws ValidationException UUID形式が無効な場合
     */
    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid UUID format: " + id);
        }
    }
}