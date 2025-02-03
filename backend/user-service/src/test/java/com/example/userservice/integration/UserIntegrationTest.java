package com.example.userservice.integration;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.userservice.UserServiceApplication;
import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = UserServiceApplication.class)
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtDecoder jwtDecoder;

	//検証用token作成
    private static final String OWNER_TOKEN_VALUE = "dummyUserToken";
    private static final String ADMIN_TOKEN_VALUE = "dummyAdminToken";
    private static final String NOT_OWNER_TOKEN_VALUE = "dummyOtherToken";

    private User existingUser;
    private CreateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // テスト用ユーザをDBに作成
        existingUser = User.builder()
            .username("existingUser")
            .email("existing@test.com")
            .password("password123")
            .build();
        existingUser = userRepository.save(existingUser);

        // 更新用のリクエストデータ
        updateRequest = CreateUserRequest.builder()
            .username("updatedUsername")
            .email("updated@test.com")
            .password("newpassword123")
            .build();

        // ユーザー用JWT (ROLE_USER)
        Jwt ownerJwt = Jwt.withTokenValue(OWNER_TOKEN_VALUE)
            .header("alg", "HS256")
            .claim("sub", "existingUser")
            .claim("scope", "ROLE_USER")
            .build();

        // 管理者用JWT (ROLE_ADMIN)
        Jwt adminJwt = Jwt.withTokenValue(ADMIN_TOKEN_VALUE)
            .header("alg", "HS256")
            .claim("sub", "adminUser")
            .claim("authorities", List.of("ROLE_ADMIN"))
            .build();

        // Forbidden用JWT (notOwnerUser)
        Jwt notOwnerJwt = Jwt.withTokenValue(NOT_OWNER_TOKEN_VALUE)
            .header("alg", "HS256")
            .claim("sub", "notOwnerUser")
            .claim("scope", "ROLE_USER")
            .build();

        Mockito.when(jwtDecoder.decode(OWNER_TOKEN_VALUE)).thenReturn(ownerJwt);
        Mockito.when(jwtDecoder.decode(ADMIN_TOKEN_VALUE)).thenReturn(adminJwt);
        Mockito.when(jwtDecoder.decode(NOT_OWNER_TOKEN_VALUE)).thenReturn(notOwnerJwt);
    }

    // 正常系テスト

    @Test
    void createUserIntegrationTest() throws Exception {
        CreateUserRequest newUser = CreateUserRequest.builder()
            .username("newuser")
            .email("newuser@test.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.email").value("newuser@test.com"));

        User createdUser = userRepository.findByUsername("newuser").orElse(null);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("newuser@test.com");
    }

    @Test
    void getUserByIdIntegrationTest() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUser.getId().toString())
                .header("Authorization", "Bearer " + OWNER_TOKEN_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(existingUser.getId().toString()))
            .andExpect(jsonPath("$.username").value("existingUser"))
            .andExpect(jsonPath("$.email").value("existing@test.com"));
    }

    @Test
    void updateUserIntegrationTest() throws Exception {
        mockMvc.perform(put("/api/users/{id}", existingUser.getId().toString())
                .header("Authorization", "Bearer " + ADMIN_TOKEN_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("updatedUsername"))
            .andExpect(jsonPath("$.email").value("updated@test.com"));

        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("updatedUsername");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void deleteUserIntegrationTest_AdminCase() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId().toString())
                .header("Authorization", "Bearer " + ADMIN_TOKEN_VALUE))
            .andExpect(status().isNoContent());

        assertThat(userRepository.findById(existingUser.getId())).isEmpty();
    }

    @Test
    void deleteUserIntegrationTest_OwnerCase() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId().toString())
                .header("Authorization", "Bearer " + OWNER_TOKEN_VALUE))
            .andExpect(status().isNoContent());

        assertThat(userRepository.findById(existingUser.getId())).isEmpty();
    }


    // 異常系テスト

    @Test
    void createUserIntegrationTest_DuplicateUsername_ShouldReturnConflict() throws Exception {
        CreateUserRequest duplicateUsernameRequest = CreateUserRequest.builder()
            .username("existingUser")
            .email("uniqueemail@test.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateUsernameRequest)))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Username existingUser is already registered"))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasItem("Username existingUser is already registered")))
            .andExpect(jsonPath("$.details.errors", hasItem("重複ユーザ: existingUser")));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void createUserIntegrationTest_DuplicateEmail_ShouldReturnConflict() throws Exception {
        User duplicateUser = User.builder()
            .username("uniqueUser")
            .email("existingemail@test.com")
            .password("password123")
            .build();
        userRepository.saveAndFlush(duplicateUser);

        CreateUserRequest duplicateEmailRequest = CreateUserRequest.builder()
            .username("newUniqueUser")
            .email("existingemail@test.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Email existingemail@test.com is already registered"))
            .andExpect(jsonPath("$.path").value("/api/users"))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasItem("Email existingemail@test.com is already registered")))
            .andExpect(jsonPath("$.details.errors", hasItem("重複ユーザ: existingemail@test.com")));

        User foundUser = userRepository.findByUsername("newUniqueUser").orElse(null);
        assertThat(foundUser).isNull();
    }

    @Test
    void getUserByIdIntegrationTest_UserNotFound_ShouldReturnNotFound() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(get("/api/users/{id}", nonExistentUserId.toString())
            .header("Authorization", "Bearer " + OWNER_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentUserId))
            .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentUserId))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasItem("User not found with id: " + nonExistentUserId)))
            .andExpect(jsonPath("$.details.errors", hasItem("ユーザID: " + nonExistentUserId.toString())));
    }

    @Test
    void updateUserIntegrationTest_UserNotFound_ShouldReturnNotFound() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(put("/api/users/{id}", nonExistentUserId)
            .header("Authorization", "Bearer " + ADMIN_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentUserId))
            .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentUserId))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasItem("User not found with id: " + nonExistentUserId)))
            .andExpect(jsonPath("$.details.errors", hasItem("ユーザID: " + nonExistentUserId.toString())));
    }

    @Test
    void deleteUserIntegrationTest_UserNotFound_ShouldReturnNotFound() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{id}", nonExistentUserId)
            .header("Authorization", "Bearer " + ADMIN_TOKEN_VALUE))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentUserId))
            .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentUserId))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasItem("User not found with id: " + nonExistentUserId)))
            .andExpect(jsonPath("$.details.errors", hasItem("ユーザID: " + nonExistentUserId.toString())));
    }

    @Test
    void getUserByIdIntegrationTest_InvalidUUID_ShouldReturnBadRequest() throws Exception {
        String invalidUuid = "invalid-uuid";

        mockMvc.perform(get("/api/users/{id}", invalidUuid)
            .header("Authorization", "Bearer " + OWNER_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Invalid UUID format: " + invalidUuid))
            .andExpect(jsonPath("$.path").value("/api/users/" + invalidUuid))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasItem("Invalid UUID format: " + invalidUuid)))
            .andExpect(jsonPath("$.details.errors", hasItem("不正なUUID: " + invalidUuid)));
    }

    @Test
    void createUserIntegrationTest_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        CreateUserRequest invalidEmailRequest = CreateUserRequest.builder()
            .username("invalidEmailUser")
            .email("invalid-email-format")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("入力値の検証に失敗しました"))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.details.errors").isArray())
                .andExpect(jsonPath("$.details.errors", hasItem("有効なEmailを入力してください。")));
    }

    @Test
    void createUserIntegrationTest_ShortPassword_ShouldReturnBadRequest() throws Exception {
        CreateUserRequest shortPasswordRequest = CreateUserRequest.builder()
            .username("shortPasswordUser")
            .email("shortpassword@test.com")
            .password("123")
            .build();

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(shortPasswordRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("入力値の検証に失敗しました"))
            .andExpect(jsonPath("$.path").value("/api/users"))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasSize(1)))
            .andExpect(jsonPath("$.details.errors", hasItem("Passwordを8文字以上で入力してください。")));
    }

    @Test
    void deleteUserIntegrationTest_Forbidden_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId().toString())
                .header("Authorization", "Bearer " + NOT_OWNER_TOKEN_VALUE))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.path").value("/api/users/" + existingUser.getId()))
            .andExpect(jsonPath("$.details.errors").isArray())
            .andExpect(jsonPath("$.details.errors", hasItem("アクセスが拒否されました")));
    }

}
