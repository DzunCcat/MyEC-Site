package com.example.userservice.controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // new code
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping; // new code
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
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
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Received request to get user with ID: {}", id);
        
        try {
            UserResponse user = userService.getUserById(id);
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
            @PathVariable Long id, 
            @Valid @RequestBody CreateUserRequest request) {
        log.info("Received request to update user with ID: {}", id);
        
        try {
            UserResponse updatedUser = userService.updateUser(id, request);
            log.info("Successfully updated user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error occurred while updating user with ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(authentication, #id)") 
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete user with ID: {}", id);
        
        try {
            userService.deleteUser(id);
            log.info("Successfully deleted user with ID: {}", id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error occurred while deleting user with ID: {}", id, e);
            throw e;
        }
    }
}
