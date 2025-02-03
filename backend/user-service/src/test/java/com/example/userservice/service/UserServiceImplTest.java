package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.error.exception.business.UserAlreadyExistsException;
import com.example.userservice.error.exception.business.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.PasswordSecurity;
import com.example.userservice.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordSecurity passwordSecurity;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private CreateUserRequest createUserRequest;
    private User user;
    private UserResponse userResponse;
    private UUID testUuid;
    
    @BeforeEach
    void setUp() {
        testUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        
        createUserRequest = CreateUserRequest.builder()
            .username("testUser")
            .email("test@example.com")
            .password("password123")
            .build();
            
        user = User.builder()
            .id(testUuid)
            .username("testUser")
            .email("test@example.com")
            .password("hashedPassword")
            .build();
            
        userResponse = UserResponse.builder()
            .id(testUuid)
            .username("testUser")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
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
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

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
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(userRepository, times(1)).existsByUsername("newUser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(testUuid)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(testUuid);

        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());
        
        verify(userRepository, times(1)).findById(testUuid);
    }

    @Test
    void getUserById_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(testUuid)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(testUuid);
        });

        assertEquals("User not found with id: " + testUuid, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(userRepository, times(1)).findById(testUuid);
    }
    
    @Test
    void updateUser_Success() {
        when(userRepository.findById(testUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.updateUser(testUuid, createUserRequest);

        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(testUuid);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(testUuid)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(testUuid, createUserRequest);
        });

        assertEquals("User not found with id: " + testUuid, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(userRepository, times(1)).findById(testUuid);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(testUuid)).thenReturn(true);
        
        userService.deleteUser(testUuid);

        verify(userRepository, times(1)).existsById(testUuid);
        verify(userRepository, times(1)).deleteById(testUuid);
    }

    @Test
    void deleteUser_ThrowsException_WhenUserNotFound() {
        when(userRepository.existsById(testUuid)).thenReturn(false);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(testUuid);
        });

        assertEquals("User not found with id: " + testUuid, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(userRepository, times(1)).existsById(testUuid);
        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteUser_ThrowsException_WhenForeignKeyConstraintViolation() {
        doThrow(new DataIntegrityViolationException("Foreign key constraint violation"))
                .when(userRepository).deleteById(testUuid);

        when(userRepository.existsById(testUuid)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.deleteUser(testUuid);
        });

        verify(userRepository, times(1)).existsById(testUuid);
        verify(userRepository, times(1)).deleteById(testUuid);
    }

    @Test
    void updateUser_ThrowsException_WhenEmailExists() {
        CreateUserRequest duplicateEmailRequest = CreateUserRequest.builder()
                .username("testUser")
                .email("existing@example.com")
                .password("password123")
                .build();

        when(userRepository.findById(testUuid)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.updateUser(testUuid, duplicateEmailRequest);
        });

        assertEquals("Email existing@example.com is already registered", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(userRepository, times(1)).findById(testUuid);
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenPasswordTooShort() {
        CreateUserRequest shortPasswordRequest = CreateUserRequest.builder()
                .username("testUser")
                .email("test@example.com")
                .password("123")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(shortPasswordRequest);
        });

        assertEquals("Password must be at least 8 characters long", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}