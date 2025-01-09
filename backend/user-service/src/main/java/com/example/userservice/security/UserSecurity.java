package com.example.userservice.security;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.business.UserNotFoundException;
import com.example.userservice.exception.validation.ValidationException;
import com.example.userservice.service.UserService;

/**
 * User関連のセキュリティチェックを提供するコンポーネント。
 * UUIDベースのUser識別子を使用して、リソースへのアクセス制御を行う。
 */
@Component("userSecurity")
public class UserSecurity {
    private static final Logger log = LoggerFactory.getLogger(UserSecurity.class);
    private final UserService userService;

    public UserSecurity(UserService userService) {
        this.userService = userService;
    }

    /**
     * 認証されたUserが指定されたUserIDのリソースの所有者であるかを確認します。
     * 
     * @param authentication 現在の認証情報
     * @param userId 確認対象のUserID（UUID形式）
     * @return 所有者である場合はtrue、そうでない場合はfalse
     * @throws UserNotFoundException Userが存在しない場合
     * @throws ValidationException UUIDが無効な形式の場合
     */
    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication check failed: authentication is null or not authenticated");
            return false;
        }

        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format provided: {}", userId);
            throw new ValidationException("Invalid UUID format: " + userId);
        }

        String currentUsername = authentication.getName();
        log.debug("Checking ownership for user: {} on resource with UUID: {}", currentUsername, userUuid);
        
        try {
            UserResponse user = userService.getUserById(userUuid);
            boolean isOwner = user.getUsername().equals(currentUsername);
            log.debug("Ownership check result for user {} on resource {}: {}", 
                     currentUsername, userUuid, isOwner);
            return isOwner;
        } catch (UserNotFoundException e) {
            log.warn("User not found during ownership check: {}", userUuid);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during ownership check", e);
            return false;
        }
    }

}