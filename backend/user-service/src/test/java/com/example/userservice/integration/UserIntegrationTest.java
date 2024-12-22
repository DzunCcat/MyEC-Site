package com.example.userservice.integration;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.userservice.UserServiceApplication;
import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(
    classes = UserServiceApplication.class
)
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class}) // Flywayの自動構成を除外
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY) // H2データベースを強制的に使用
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User existingUser;
    private CreateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        // テスト用データのクリーンアップ
        userRepository.deleteAll();

        // テストユーザーの作成
        existingUser = User.builder()
                .username("existingUser")
                .email("existing@test.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        existingUser = userRepository.save(existingUser);
        // エンティティマネージャーを使用して即時フラッシュ（必要に応じて）
        // entityManager.flush();

        // 更新用のリクエストデータを準備
        updateRequest = CreateUserRequest.builder()
                .username("updatedUsername")
                .email("updated@test.com")
                .password("newpassword123")
                .build();
    }

    // 正常系テスト

    @Test
    @WithMockUser
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

        // データベースの状態を確認
        User createdUser = userRepository.findByUsername("newuser").orElse(null);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("newuser@test.com");
    }

    @Test
    @WithMockUser
    void getUserByIdIntegrationTest() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("existingUser"))
                .andExpect(jsonPath("$.email").value("existing@test.com"));
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void updateUserIntegrationTest() throws Exception {
        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("updatedUsername"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        // データベースの状態を確認
        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("updatedUsername");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void deleteUserIntegrationTest() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(existingUser.getId())).isEmpty();
    }

    // 異常系テスト

    @Test
    @WithMockUser
    void createUserIntegrationTest_DuplicateUsername_ShouldReturnConflict() throws Exception {
        // @BeforeEachですでに "existingUser" は作成済みなので、
        // 直接のリポジトリ操作は不要です

        // APIを通じて重複ユーザー作成を試みる
        CreateUserRequest duplicateUsernameRequest = CreateUserRequest.builder()
            .username("existingUser")          // @BeforeEachで作成済みのユーザー名
            .email("uniqueemail@test.com")
            .password("password123")
            .build();

        // APIエンドポイントを通じてテスト
        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateUsernameRequest)))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message")
                .value("Username existingUser is already registered"));

        // データベースの状態確認
        assertThat(userRepository.count()).isEqualTo(1);  // ユーザーが1人だけ存在することを確認
    }

    @Test
    @WithMockUser
    void createUserIntegrationTest_DuplicateEmail_ShouldReturnConflict() throws Exception {
        // 既存のユーザーを保存
        User duplicateUser = User.builder()
                .username("uniqueUser")
                .email("existingemail@test.com") // 既に存在するメールアドレス
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.saveAndFlush(duplicateUser);

        // 重複するメールアドレスを持つ新規ユーザーのリクエスト
        CreateUserRequest duplicateEmailRequest = CreateUserRequest.builder()
                .username("newUniqueUser")
                .email("existingemail@test.com") // 重複するメールアドレス
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
                .andExpect(jsonPath("$.details").doesNotExist());

        // データベースの状態を確認
        User foundUser = userRepository.findByUsername("newUniqueUser").orElse(null);
        assertThat(foundUser).isNull();
    }

    @Test
    @WithMockUser
    void getUserByIdIntegrationTest_UserNotFound_ShouldReturnNotFound() throws Exception {
        Long nonExistentUserId = 999L;

        mockMvc.perform(get("/api/users/{id}", nonExistentUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/users/999"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser
    void updateUserIntegrationTest_UserNotFound_ShouldReturnNotFound() throws Exception {
        Long nonExistentUserId = 999L;

        mockMvc.perform(put("/api/users/{id}", nonExistentUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/users/999"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser
    void deleteUserIntegrationTest_UserNotFound_ShouldReturnNotFound() throws Exception {
        Long nonExistentUserId = 999L;

        mockMvc.perform(delete("/api/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/users/999"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser
    void createUserIntegrationTest_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        CreateUserRequest invalidEmailRequest = CreateUserRequest.builder()
                .username("invalidEmailUser")
                .email("invalid-email-format") // 無効なメール形式
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("入力値の検証に失敗しました"))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(1))) // 具体的なエラーメッセージ数に応じて調整
                .andExpect(jsonPath("$.details[0]").value("有効なEmailを入力してください。"));
    }

    @Test
    @WithMockUser
    void createUserIntegrationTest_ShortPassword_ShouldReturnBadRequest() throws Exception {
        CreateUserRequest shortPasswordRequest = CreateUserRequest.builder()
                .username("shortPasswordUser")
                .email("shortpassword@test.com")
                .password("123") // 短すぎるパスワード
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
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(1))) // 具体的なエラーメッセージ数に応じて調整
                .andExpect(jsonPath("$.details[0]").value("Passwordを8文字以上で入力してください。"));
    }

    @Test
    @WithMockUser(username = "regularUser", roles = {"USER"})
    void deleteUserIntegrationTest_Forbidden_ShouldReturnForbidden() throws Exception {
        // 管理者権限が必要なエンドポイントに対して一般ユーザーがアクセス
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.path").value("/api/users/" + existingUser.getId()))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

}
