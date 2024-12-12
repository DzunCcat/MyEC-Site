package com.example.userservice.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

//api-gatewayへのresponse

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}