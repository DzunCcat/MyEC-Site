////package com.example.userservice.integration;
////
////import static org.assertj.core.api.Assertions.*;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
////
////import jakarta.persistence.EntityManager;
////import jakarta.persistence.PersistenceContext;
////
////import org.junit.jupiter.api.BeforeEach;
////import org.junit.jupiter.api.Test;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
////import org.springframework.boot.test.context.SpringBootTest;
////import org.springframework.http.MediaType;
////import org.springframework.security.test.context.support.WithMockUser;
////import org.springframework.test.context.ActiveProfiles;
////import org.springframework.test.web.servlet.MockMvc;
////import org.springframework.transaction.annotation.Transactional;
////
////import com.example.userservice.dto.request.CreateUserRequest;
////import com.example.userservice.entity.User;
////import com.example.userservice.repository.UserRepository;
////import com.fasterxml.jackson.databind.ObjectMapper;
////
////@SpringBootTest
////@AutoConfigureMockMvc
////@ActiveProfiles("test")
////@Transactional
////public class UserIntegrationTest {
////
////    @Autowired
////    private MockMvc mockMvc;
////
////    @Autowired
////    private UserRepository userRepository;
////
////    @Autowired
////    private ObjectMapper objectMapper;
////
////    @PersistenceContext
////    private EntityManager entityManager;
////
////    private User existingUser;
////    private CreateUserRequest updateRequest;
////
////    @BeforeEach
////    void setUp() {
////        // テストデータのクリーンアップ
////        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
////        entityManager.createNativeQuery("TRUNCATE TABLE users").executeUpdate();
////        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
////        entityManager.flush();
////
////        // テスト用の既存ユーザーを作成
////        existingUser = User.builder()
////                .username("existingUser")
////                .email("existing@test.com")
////                .password("password123")
////                .build();
////        existingUser = userRepository.save(existingUser);
////
////        // 更新用のリクエストデータを準備
////        updateRequest = CreateUserRequest.builder()
////                .username("updatedUsername")
////                .email("updated@test.com")
////                .password("newpassword123")
////                .build();
////    }
////
////    @Test
////    @WithMockUser
////    void getUserByIdIntegrationTest() throws Exception {
////        // 実行
////        mockMvc.perform(get("/api/users/{id}", existingUser.getId())
////                .contentType(MediaType.APPLICATION_JSON))
////                .andExpect(status().isOk())
////                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
////                .andExpect(jsonPath("$.id").value(existingUser.getId()))
////                .andExpect(jsonPath("$.username").value(existingUser.getUsername()))
////                .andExpect(jsonPath("$.email").value(existingUser.getEmail()));
////    }
////
////    @Test
////    @WithMockUser
////    void updateUserIntegrationTest() throws Exception {
////        // 実行
////        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(objectMapper.writeValueAsString(updateRequest)))
////                .andExpect(status().isOk())
////                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
////                .andExpect(jsonPath("$.username").value(updateRequest.getUsername()))
////                .andExpect(jsonPath("$.email").value(updateRequest.getEmail()));
////
////        // データベースの状態を検証
////        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
////        assertThat(updatedUser.getUsername()).isEqualTo(updateRequest.getUsername());
////        assertThat(updatedUser.getEmail()).isEqualTo(updateRequest.getEmail());
////    }
////
////    @Test
////    @WithMockUser
////    void deleteUserIntegrationTest() throws Exception {
////        // 削除リクエストを実行
////        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
////                .andExpect(status().isNoContent());
////
////        // データベースから削除されたことを確認
////        entityManager.flush();
////        entityManager.clear();
////        
////        assertThat(userRepository.findById(existingUser.getId())).isEmpty();
////    }
////
////    @Test
////    void createUserIntegrationTest() throws Exception {
////        CreateUserRequest newUser = CreateUserRequest.builder()
////                .username("newuser")
////                .email("newuser@test.com")
////                .password("password123")
////                .build();
////
////        mockMvc.perform(post("/api/users")
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(objectMapper.writeValueAsString(newUser)))
////                .andExpect(status().isCreated())
////                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
////                .andExpect(jsonPath("$.username").value(newUser.getUsername()))
////                .andExpect(jsonPath("$.email").value(newUser.getEmail()));
////    }
////}
//
//package com.example.userservice.integration;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.example.userservice.dto.request.CreateUserRequest;
//import com.example.userservice.entity.User;
//import com.example.userservice.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//public class UserIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    private User existingUser;
//    private CreateUserRequest updateRequest;
//
//    @BeforeEach
//    void setUp() {
//        // データベースのクリーンアップ
//        userRepository.deleteAll();
//        entityManager.flush();
//        entityManager.clear();
//
//        // テストユーザーの作成
//        existingUser = User.builder()
//                .username("existingUser")
//                .email("existing@test.com")
//                .password("password123")
//                .build();
//        existingUser = userRepository.save(existingUser);
//        entityManager.flush();
//
//        // 更新用リクエストの準備
//        updateRequest = CreateUserRequest.builder()
//                .username("updatedUsername")
//                .email("updated@test.com")
//                .password("newpassword123")
//                .build();
//    }
//
//    @Test
//    void createUserIntegrationTest() throws Exception {
//        CreateUserRequest newUser = CreateUserRequest.builder()
//                .username("newuser")
//                .email("newuser@test.com")
//                .password("password123")
//                .build();
//
//        mockMvc.perform(post("/api/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(newUser)))
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.username").value(newUser.getUsername()))
//                .andExpect(jsonPath("$.email").value(newUser.getEmail()));
//    }
//
//    @Test
//    @WithMockUser
//    void getUserByIdIntegrationTest() throws Exception {
//        mockMvc.perform(get("/api/users/{id}", existingUser.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(existingUser.getId()))
//                .andExpect(jsonPath("$.username").value(existingUser.getUsername()))
//                .andExpect(jsonPath("$.email").value(existingUser.getEmail()));
//    }
//
//    @Test
//    @WithMockUser
//    void updateUserIntegrationTest() throws Exception {
//        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.username").value(updateRequest.getUsername()))
//                .andExpect(jsonPath("$.email").value(updateRequest.getEmail()));
//
//        // データベースの状態を確認
//        entityManager.flush();
//        entityManager.clear();
//        
//        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
//        assertThat(updatedUser.getUsername()).isEqualTo(updateRequest.getUsername());
//        assertThat(updatedUser.getEmail()).isEqualTo(updateRequest.getEmail());
//    }
//
//    @Test
//    @WithMockUser
//    void deleteUserIntegrationTest() throws Exception {
//        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
//                .andExpect(status().isNoContent());
//
//        entityManager.flush();
//        entityManager.clear();
//        
//        assertThat(userRepository.findById(existingUser.getId())).isEmpty();
//    }
//}
package com.example.userservice.integration;

import static org.assertj.core.api.Assertions.*;
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

        // 更新用のリクエストデータを準備
        updateRequest = CreateUserRequest.builder()
                .username("updatedUsername")
                .email("updated@test.com")
                .password("newpassword123")
                .build();
    }

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
    @WithMockUser
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
        assertThat(updatedUser.getUsername()).isEqualTo(updateRequest.getUsername());
        assertThat(updatedUser.getEmail()).isEqualTo(updateRequest.getEmail());
    }

    @Test
    @WithMockUser
    void deleteUserIntegrationTest() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(existingUser.getId())).isEmpty();
    }
}
