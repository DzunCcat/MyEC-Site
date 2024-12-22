package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

//JUnitの基本的なアノテーションとアサーション用のインポート
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//Mockitoの機能を使用するためのインポート
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.exception.business.UserAlreadyExistsException;
import com.example.userservice.exception.business.UserNotFoundException;
//テスト対象のクラスとその依存関係のインポート
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private CreateUserRequest createUserRequest;
    private User user;
    private UserResponse userResponse;
    
    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
            .username("testUser")
            .email("test@example.com")
            .password("password123")
            .build();
            
        user = User.builder()
            .id(1L)
            .username("testUser")
            .email("test@example.com")
            .password("hashedPassword")
            .build();
            
        userResponse = UserResponse.builder()
            .id(1L)
            .username("testUser")
            .email("test@example.com")
            .build();
    }

    @Test
    void createUser_Success() {
    	
        when(userRepository.save(any(User.class)))
            .thenReturn(user);

        UserResponse result = userService.createUser(createUserRequest);

        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenUsernameExists() {

        when(userRepository.existsByUsername(anyString()))
            .thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(createUserRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void getUserById_Success() {
        // ユーザーが見つかる場合のテスト
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        // 返されたユーザー情報が正しいか検証
        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());
        
        // findByIdが1回呼ばれたことを検証
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ThrowsException_WhenUserNotFound() {
        // ユーザーが見つからない場合のテスト
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // UserNotFoundExceptionがスローされることを検証
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(1L);
        });

        verify(userRepository, times(1)).findById(1L);
    }
    
    
    @Test
    void updateUser_Success() {
        // 更新対象のユーザーが存在する場合のテスト
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.updateUser(1L, createUserRequest);

        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ThrowsException_WhenUserNotFound() {
        // 更新対象のユーザーが存在しない場合のテスト
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(1L, createUserRequest);
        });

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void deleteUser_Success() {
        // 削除対象のユーザーが存在する場合のテスト
        when(userRepository.existsById(1L)).thenReturn(true);
        
        userService.deleteUser(1L);

        // existsByIdとdeleteByIdが各1回呼ばれたことを検証
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ThrowsException_WhenUserNotFound() {
        // 削除対象のユーザーが存在しない場合のテスト
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(1L);
    }


}