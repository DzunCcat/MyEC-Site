package com.example.userservice.service;

import java.util.List;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, CreateUserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
}
