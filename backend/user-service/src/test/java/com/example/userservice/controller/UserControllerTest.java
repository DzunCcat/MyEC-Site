package com.example.userservice.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import; // new code
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles; // new code
import org.springframework.test.web.servlet.MockMvc;

import com.example.userservice.config.SecurityConfig; // new code
import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.business.UserAlreadyExistsException;
import com.example.userservice.exception.business.UserNotFoundException;
import com.example.userservice.exception.handler.GlobalExceptionHandler; // new code
import com.example.userservice.security.UserSecurity; // new code
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class}) // new code: SecurityConfigとGlobalExceptionHandlerをインポート
@ActiveProfiles("test") // new code: テストプロファイルを有効化
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "userSecurity")
    private UserSecurity userSecurity;

    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
            .username("testUser")
            .email("test@example.com")
            .password("password123")
            .build();

        userResponse = UserResponse.builder()
            .id(1L)
            .username("testUser")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void createUser_Success() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
            .thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateUser_Success() throws Exception {
        when(userService.updateUser(eq(1L), any(CreateUserRequest.class)))
            .thenReturn(userResponse);

        // new code: 所有者判定true
        when(userSecurity.isOwner(any(), eq(1L))).thenReturn(true);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void createUser_ValidationFailed() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
            .username("")
            .email("invalid-email")
            .password("123")
            .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("入力値の検証に失敗しました"))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(3)))
                .andExpect(jsonPath("$.details", containsInAnyOrder(
                    "Usernameは必須です。",
                    "有効なEmailを入力してください。",
                    "Passwordを8文字以上で入力してください。"
                )));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(1L))
            .thenThrow(new UserNotFoundException("User not found with id: 1"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 1"))
                .andExpect(jsonPath("$.path").value("/api/users/1"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void createUser_UserAlreadyExists() throws Exception {
        CreateUserRequest duplicateUserRequest = CreateUserRequest.builder()
            .username("existingUser")
            .email("existing@example.com")
            .password("password123")
            .build();

        when(userService.createUser(any(CreateUserRequest.class)))
            .thenThrow(new UserAlreadyExistsException("Username existingUser is already registered"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username existingUser is already registered"))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateUser_NotFound() throws Exception {
        when(userService.updateUser(eq(1L), any(CreateUserRequest.class)))
            .thenThrow(new UserNotFoundException("User not found with id: 1"));

        // new code: 所有者判定false
        when(userSecurity.isOwner(any(), eq(1L))).thenReturn(true);


        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 1"))
                .andExpect(jsonPath("$.path").value("/api/users/1"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getUserById_InternalServerError() throws Exception {
        when(userService.getUserById(1L))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("予期せぬエラーが発生しました"))
                .andExpect(jsonPath("$.path").value("/api/users/1"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void getUserById_Unauthorized() throws Exception {
        // 認証情報なしでアクセス -> 401
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"))
                .andExpect(jsonPath("$.path").value("/api/users/1"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteUser_Forbidden() throws Exception {
        // ADMINまたは所有者でない場合 -> 403
        when(userSecurity.isOwner(any(), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.path").value("/api/users/1"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }
}
