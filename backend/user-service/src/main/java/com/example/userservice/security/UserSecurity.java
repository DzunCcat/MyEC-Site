package com.example.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.business.UserNotFoundException;
import com.example.userservice.service.UserService;

@Component("userSecurity")
public class UserSecurity {
    private final UserService userService;

    public UserSecurity(UserService userService) {
        this.userService = userService;
    }

    public boolean isOwner(Authentication authentication, Long userId) {
        // 認証チェック
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        
        try {
            // ユーザーの取得を試みる
            UserResponse user = userService.getUserById(userId);
            // ユーザーが存在する場合のみ、所有者チェックを実行
            return user.getUsername().equals(currentUsername);
        } catch (UserNotFoundException e) {
            // UserNotFoundExceptionを再スローして、404レスポンスを発生させる
            throw e;
        } catch (Exception e) {
            // その他の予期せぬエラーの場合は所有者ではないとみなす
            return false;
        }
    }
}