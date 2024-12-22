package com.example.userservice.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.exception.business.UserAlreadyExistsException;
import com.example.userservice.exception.business.UserNotFoundException;
import com.example.userservice.exception.validation.ValidationException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());
        
        validateNewUser(request);

        try {
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                    .build();

            User savedUser = userRepository.save(user);
            log.info("Successfully created user with ID: {}", savedUser.getId());
            
            return mapToResponse(savedUser);
        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw new ValidationException("User creation failed");
        }
    }

    @Override
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attempted to update non-existent user with ID: {}", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });
        
        validateUpdateUser(request, existingUser);

        try {
            existingUser.setUsername(request.getUsername());
            existingUser.setEmail(request.getEmail());
            if (request.getPassword() != null) {
                existingUser.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            }

            User updatedUser = userRepository.save(existingUser);
            log.info("Successfully updated user with ID: {}", id);
            
            return mapToResponse(updatedUser);
        } catch (Exception e) {
            log.error("Failed to update user with ID: {}", id, e);
            throw new ValidationException("User update failed.");
        }
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attempted to fetch non-existent user with ID: {}", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });

        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        
        List<User> users = userRepository.findAll();
        log.info("Found {} users", users.size());
        
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent user with ID: {}", id);
            throw new UserNotFoundException("User not found with id: " + id);
        }
        
        try {
            userRepository.deleteById(id);
            log.info("Successfully deleted user with ID: {}", id);
        } catch (DataIntegrityViolationException e) {
            log.error("Foreign key constraint violation when deleting user with ID: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting user with ID: {}", id, e);
            throw new ValidationException("Failed to delete user");
        }
    }

    private void validateNewUser(CreateUserRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            log.warn("Attempted to create user with password shorter than 8 characters");
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    	
    	
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Attempted to create user with existing username: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username " + request.getUsername() + " is already registered");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Attempted to create user with existing email: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email " + request.getEmail() + " is already registered");
        }
    }

    private void validateUpdateUser(CreateUserRequest request, User existingUser) {
        if (!request.getUsername().equals(existingUser.getUsername()) &&
            userRepository.existsByUsername(request.getUsername())) {
            log.warn("Attempted to update user with existing username: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username " + request.getUsername() + " is already registered");
        }

        if (!request.getEmail().equals(existingUser.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            log.warn("Attempted to update user with existing email: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email " + request.getEmail() + " is already registered");
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}