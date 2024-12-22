package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.exception.business.UserAlreadyExistsException;
import com.example.userservice.exception.business.UserNotFoundException;
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

    // 正常系テスト
    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.createUser(createUserRequest);

        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByUsername("testUser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenUsernameExists() {
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(createUserRequest);
        });

        assertEquals("Username testUser is already registered", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername("testUser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenEmailExists() {
        CreateUserRequest duplicateEmailRequest = CreateUserRequest.builder()
                .username("newUser")
                .email("test@example.com") 
                .password("password123")
                .build();

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(duplicateEmailRequest);
        });

        assertEquals("Email test@example.com is already registered", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername("newUser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());
        
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(1L);
        });

        assertEquals("User not found with id: 1", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    void updateUser_Success() {
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
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(1L, createUserRequest);
        });

        assertEquals("User not found with id: 1", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ThrowsException_WhenEmailExists() {
        CreateUserRequest duplicateEmailRequest = CreateUserRequest.builder()
                .username("testUser")
                .email("existing@example.com")
                .password("password123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.updateUser(1L, duplicateEmailRequest);
        });

        assertEquals("Email existing@example.com is already registered", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        
        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ThrowsException_WhenUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        assertEquals("User not found with id: 1", exception.getMessage());

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_ThrowsException_WhenForeignKeyConstraintViolation() {
        // モックの設定: ユーザー削除時にデータベースが例外をスローする
        doThrow(new DataIntegrityViolationException("Foreign key constraint violation"))
                .when(userRepository).deleteById(1L);

        // 削除対象のユーザーが存在すると仮定
        when(userRepository.existsById(1L)).thenReturn(true);

        // DataIntegrityViolationExceptionがスローされることを検証
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.deleteUser(1L);
        });

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    // パスワードのバリデーションに関する異常系テスト
    @Test
    void createUser_ThrowsException_WhenPasswordTooShort() {
        // パスワードが短すぎるリクエスト
        CreateUserRequest shortPasswordRequest = CreateUserRequest.builder()
                .username("testUser")
                .email("test@example.com")
                .password("123") // 短すぎるパスワード
                .build();

        // UserServiceImplがパスワードの長さを検証していると仮定
        // 実装に応じて適切に設定します
        // 例えば、IllegalArgumentExceptionをスローする場合
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(shortPasswordRequest);
        });

        // saveメソッドが呼ばれないことを検証
        verify(userRepository, never()).save(any(User.class));
    }
}
