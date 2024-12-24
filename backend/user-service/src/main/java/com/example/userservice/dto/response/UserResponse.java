package com.example.userservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

// api-gatewayへのresponse
// このDTOクラスは、ユーザー情報をクライアントに返す際のレスポンス形式を定義します。
// UUIDを使用することで、マイクロサービス環境での一意性を保証します。
@Data
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}