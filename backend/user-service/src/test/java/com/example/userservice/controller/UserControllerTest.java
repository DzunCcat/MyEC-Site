package com.example.userservice.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.userservice.config.SecurityConfig;
import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.business.UserAlreadyExistsException;
import com.example.userservice.exception.business.UserNotFoundException;
import com.example.userservice.exception.handler.GlobalExceptionHandler;
import com.example.userservice.security.UserSecurity;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "userSecurity")
    private UserSecurity userSecurity;

    @MockBean
    private JwtDecoder jwtDecoder;

    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;
    private UUID testUuid;

    private static final String USER_TOKEN_VALUE = "dummyUserToken";
    private static final String ADMIN_TOKEN_VALUE = "dummyAdminToken";

    @BeforeEach
    void setUp() {
        testUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        createUserRequest = CreateUserRequest.builder()
            .username("testUser")
            .email("test@example.com")
            .password("password123")
            .build();

        userResponse = UserResponse.builder()
            .id(testUuid)
            .username("testUser")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Jwt userJwt = Jwt.withTokenValue(USER_TOKEN_VALUE)
            .header("alg", "HS256")
            .claim("sub", "testuser")         
            .claim("scope", "ROLE_USER")     
            .build();

        Jwt adminJwt = Jwt.withTokenValue(ADMIN_TOKEN_VALUE)
            .header("alg", "HS256")
            .claim("sub", "adminuser")
            .claim("authorities", List.of("ROLE_ADMIN"))
            .build();

        when(jwtDecoder.decode(USER_TOKEN_VALUE)).thenReturn(userJwt);
        when(jwtDecoder.decode(ADMIN_TOKEN_VALUE)).thenReturn(adminJwt);
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
                .andExpect(jsonPath("$.id").value(testUuid.toString()))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(any(UUID.class))).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/" + testUuid)
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUuid.toString()))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

	//user delete
    @Test
    void deleteUser_SuccessAsOwner() throws Exception {
        doNothing().when(userService).deleteUser(any(UUID.class));
        when(userSecurity.isOwner(any(), eq(testUuid.toString()))).thenReturn(true);

        mockMvc.perform(delete("/api/users/" + testUuid)
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isNoContent());
    }

	//admin delete
    @Test
    void deleteUser_SuccessAsAdmin() throws Exception {
        doNothing().when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/api/users/" + testUuid)
                .header("Authorization", "Bearer " + ADMIN_TOKEN_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userService.updateUser(any(UUID.class), any(CreateUserRequest.class)))
            .thenReturn(userResponse);

        when(userSecurity.isOwner(any(), eq(testUuid.toString()))).thenReturn(true);

        mockMvc.perform(put("/api/users/" + testUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUuid.toString()))
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
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(any(UUID.class)))
            .thenThrow(new UserNotFoundException("User not found with id: " + testUuid));

        mockMvc.perform(get("/api/users/" + testUuid)
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: " + testUuid))
                .andExpect(jsonPath("$.path").value("/api/users/" + testUuid))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void getUserById_InvalidUUID() throws Exception {
        mockMvc.perform(get("/api/users/" + "invalid-uuid")
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid UUID format: invalid-uuid"))
                .andExpect(jsonPath("$.path").value("/api/users/invalid-uuid"))
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
    void updateUser_NotFound() throws Exception {
        when(userService.updateUser(any(UUID.class), any(CreateUserRequest.class)))
            .thenThrow(new UserNotFoundException("User not found with id: " + testUuid));

        when(userSecurity.isOwner(any(), eq(testUuid.toString()))).thenReturn(true);

        mockMvc.perform(put("/api/users/" + testUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: " + testUuid))
                .andExpect(jsonPath("$.path").value("/api/users/" + testUuid))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void getUserById_InternalServerError() throws Exception {
        when(userService.getUserById(any(UUID.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/users/" + testUuid)
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("予期せぬエラーが発生しました"))
                .andExpect(jsonPath("$.path").value("/api/users/" + testUuid))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void getUserById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/" + testUuid))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"))
                .andExpect(jsonPath("$.path").value("/api/users/" + testUuid))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void deleteUser_Forbidden() throws Exception {
        when(userSecurity.isOwner(any(), eq(testUuid.toString()))).thenReturn(false);

        mockMvc.perform(delete("/api/users/" + testUuid)
                .header("Authorization", "Bearer " + USER_TOKEN_VALUE))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.path").value("/api/users/" + testUuid))
                .andExpect(jsonPath("$.details").doesNotExist());
    }
}
