////package com.example.userservice.repository;
////
////import static org.assertj.core.api.Assertions.*;
////
////import java.util.Optional;
////
////import org.junit.jupiter.api.BeforeEach;
////import org.junit.jupiter.api.Test;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
////import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
////import org.springframework.test.context.ActiveProfiles;
////import org.springframework.test.context.TestPropertySource;
////
////import com.example.userservice.entity.User;
////
////@DataJpaTest
////@ActiveProfiles("test")
////@TestPropertySource(locations = "classpath:application-test.yml")
////@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
////class UserRepositoryTest {
////    @Autowired
////    private UserRepository userRepository;
////
////    private User testUser;
////
////    @BeforeEach
////    void setUp() {
////        // テストデータのセットアップ
////        testUser = User.builder()
////            .username("testUser")
////            .email("test@example.com")
////            .password("hashedPassword")
////            .build();
////    }
////
////    @Test
////    void saveUser_Success() {
////        // ユーザーの保存をテスト
////        User savedUser = userRepository.save(testUser);
////
////        // 保存されたユーザーの検証
////        assertThat(savedUser.getId()).isNotNull();
////        assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername());
////        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
////        assertThat(savedUser.getPassword()).isEqualTo(testUser.getPassword());
////    }
////
////    @Test
////    void findByUsername_Success() {
////        // テストデータを保存
////        userRepository.save(testUser);
////
////        // ユーザー名による検索をテスト
////        Optional<User> foundUser = userRepository.findByUsername(testUser.getUsername());
////        
////        // 検索結果の検証
////        assertThat(foundUser).isPresent();
////        assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
////        assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
////    }
////
////    @Test
////    void findByUsername_ReturnEmpty_WhenUserNotFound() {
////        // 存在しないユーザー名での検索をテスト
////        Optional<User> foundUser = userRepository.findByUsername("nonexistent");
////        
////        // 結果が空であることを検証
////        assertThat(foundUser).isEmpty();
////    }
////
////    @Test
////    void existsByUsername_ReturnTrue_WhenExists() {
////        // テストデータを保存
////        userRepository.save(testUser);
////
////        // ユーザー名の存在チェックをテスト
////        boolean exists = userRepository.existsByUsername(testUser.getUsername());
////        
////        // 結果の検証
////        assertThat(exists).isTrue();
////    }
////
////    @Test
////    void existsByUsername_ReturnFalse_WhenNotExists() {
////        // 存在しないユーザー名でのチェックをテスト
////        boolean exists = userRepository.existsByUsername("nonexistent");
////        
////        // 結果の検証
////        assertThat(exists).isFalse();
////    }
////
////    @Test
////    void existsByEmail_ReturnTrue_WhenExists() {
////        // テストデータを保存
////        userRepository.save(testUser);
////
////        // メールアドレスの存在チェックをテスト
////        boolean exists = userRepository.existsByEmail(testUser.getEmail());
////        
////        // 結果の検証
////        assertThat(exists).isTrue();
////    }
////
////    @Test
////    void deleteUser_Success() {
////        // テストデータを保存
////        User savedUser = userRepository.save(testUser);
////
////        // ユーザーの削除をテスト
////        userRepository.deleteById(savedUser.getId());
////
////        // 削除の確認
////        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
////        assertThat(deletedUser).isEmpty();
////    }
////}
//
//package com.example.userservice.repository;
//
//import static org.assertj.core.api.Assertions.*;
//
//import java.time.LocalDateTime;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.test.context.ActiveProfiles;
//
//import com.example.userservice.entity.User;
//
//@DataJpaTest
//@ActiveProfiles("test")
//class UserRepositoryTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        testUser = User.builder()
//                .username("testuser")
//                .email("test@example.com")
//                .password("password123")
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
//
//    @Test
//    void saveUser_Success() {
//        // テストユーザーを保存
//        User savedUser = userRepository.save(testUser);
//        
//        // 永続化を確実にする
//        entityManager.flush();
//        entityManager.clear();
//
//        // 検証
//        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
//        assertThat(foundUser).isNotNull();
//        assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
//        assertThat(foundUser.getEmail()).isEqualTo(testUser.getEmail());
//    }
//
//    @Test
//    void findByUsername_Success() {
//        // テストデータを準備
//        entityManager.persist(testUser);
//        entityManager.flush();
//        
//        // テスト実行
//        User foundUser = userRepository.findByUsername(testUser.getUsername()).orElse(null);
//        
//        // 検証
//        assertThat(foundUser).isNotNull();
//        assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
//    }
//
//    @Test
//    void findByUsername_ReturnEmpty_WhenUserNotFound() {
//        // テスト実行
//        var result = userRepository.findByUsername("nonexistent");
//        
//        // 検証
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    void existsByUsername_ReturnTrue_WhenExists() {
//        // テストデータを準備
//        entityManager.persist(testUser);
//        entityManager.flush();
//        
//        // 検証
//        assertThat(userRepository.existsByUsername(testUser.getUsername())).isTrue();
//    }
//
//    @Test
//    void existsByUsername_ReturnFalse_WhenNotExists() {
//        // 検証
//        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
//    }
//
//    @Test
//    void deleteUser_Success() {
//        // テストデータを準備
//        User savedUser = entityManager.persist(testUser);
//        entityManager.flush();
//        
//        // 削除実行
//        userRepository.deleteById(savedUser.getId());
//        entityManager.flush();
//        
//        // 検証
//        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
//    }
//}

package com.example.userservice.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.userservice.entity.User;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class) // Flywayの自動構成を除外
@AutoConfigureTestDatabase(replace = Replace.ANY) // H2データベースを強制的に使用
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByUsername_ReturnTrue_WhenExists() {
        User user = User.builder()
                .username("testuser")
                .email("testuser@test.com")
                .password("password")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("testuser");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ReturnFalse_WhenNotExists() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    void findByUsername_Success() {
        User user = User.builder()
                .username("finduser")
                .email("finduser@test.com")
                .password("password")
                .build();
        userRepository.save(user);

        User found = userRepository.findByUsername("finduser").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("finduser@test.com");
    }

    @Test
    void findByUsername_ReturnEmpty_WhenUserNotFound() {
        User found = userRepository.findByUsername("unknown").orElse(null);
        assertThat(found).isNull();
    }

    @Test
    void saveUser_Success() {
        User user = User.builder()
                .username("saveuser")
                .email("saveuser@test.com")
                .password("password")
                .build();
        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void deleteUser_Success() {
        User user = User.builder()
                .username("deleteuser")
                .email("deleteuser@test.com")
                .password("password")
                .build();
        User saved = userRepository.save(user);
        userRepository.delete(saved);

        boolean exists = userRepository.existsById(saved.getId());
        assertThat(exists).isFalse();
    }
}
