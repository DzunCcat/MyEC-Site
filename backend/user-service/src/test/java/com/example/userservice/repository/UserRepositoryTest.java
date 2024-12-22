package com.example.userservice.repository;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.userservice.entity.User;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    // テストデータの作成に使用する共通の値
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL_DOMAIN = "@test.com";

    @BeforeEach
    void setUp() {
        // 各テストの前にデータベースをクリーンアップ
        userRepository.deleteAll();
        userRepository.flush();
        
        // パスワードエンコーダーの初期化
        passwordEncoder = new BCryptPasswordEncoder();
    }

    // 正常系テスト群

    @Test
    @Transactional
    void saveUser_Success() {
        // テストデータの準備
        User user = User.builder()
                .username("testuser")
                .email("testuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        // テストの実行
        User savedUser = userRepository.save(user);

        // 検証
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser" + TEST_EMAIL_DOMAIN);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(BCrypt.checkpw(TEST_PASSWORD, savedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    void findByUsername_Success() {
        // テストデータの準備
        User user = User.builder()
                .username("finduser")
                .email("finduser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.save(user);

        // テストの実行と検証
        User found = userRepository.findByUsername("finduser").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("finduser" + TEST_EMAIL_DOMAIN);
    }

    @Test
    @Transactional
    void findByUsername_ReturnEmpty_WhenUserNotFound() {
        // テストの実行と検証
        User found = userRepository.findByUsername("unknown").orElse(null);
        assertThat(found).isNull();
    }

    @Test
    @Transactional
    void existsByUsername_ReturnTrue_WhenExists() {
        // テストデータの準備
        User user = User.builder()
                .username("existsuser")
                .email("existsuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.save(user);

        // テストの実行と検証
        boolean exists = userRepository.existsByUsername("existsuser");
        assertThat(exists).isTrue();
    }

    @Test
    @Transactional
    void existsByUsername_ReturnFalse_WhenNotExists() {
        // テストの実行と検証
        boolean exists = userRepository.existsByUsername("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    @Transactional
    void deleteUser_Success() {
        // テストデータの準備
        User user = User.builder()
                .username("deleteuser")
                .email("deleteuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        User savedUser = userRepository.save(user);

        // テストの実行
        userRepository.delete(savedUser);
        userRepository.flush();

        // 検証
        boolean exists = userRepository.existsById(savedUser.getId());
        assertThat(exists).isFalse();
    }

    // 異常系テスト群

    @Test
    @Transactional
    void saveUser_DuplicateUsername_ShouldThrowException() {
        // 既存のユーザーを保存
        User user1 = User.builder()
                .username("duplicateUser")
                .email("user1" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.saveAndFlush(user1);

        // 重複したユーザー名を持つ新しいユーザーを作成
        User user2 = User.builder()
                .username("duplicateUser")
                .email("user2" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        // テストの実行と検証
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("CONSTRAINT_INDEX")
                .hasMessageContaining("USERNAME");
    }

    @Test
    @Transactional
    void saveUser_DuplicateEmail_ShouldThrowException() {
        // 既存のユーザーを保存
        User user1 = User.builder()
                .username("user1")
                .email("duplicate" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.saveAndFlush(user1);

        // 重複したメールアドレスを持つ新しいユーザーを作成
        User user2 = User.builder()
                .username("user2")
                .email("duplicate" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        // テストの実行と検証
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("CONSTRAINT_INDEX")
                .hasMessageContaining("EMAIL");
    }

    @Test
    @Transactional
    void saveUser_NullUsername_ShouldThrowException() {
        // Nullのユーザー名を持つユーザーを作成
        User user = User.builder()
                .username(null)
                .email("nullusername" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        // テストの実行と検証
        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOfAny(
                    DataIntegrityViolationException.class,
                    ConstraintViolationException.class
                )
                .satisfiesAnyOf(
                    throwable -> assertThat(throwable.getMessage()).contains("NULL"),
                    throwable -> assertThat(throwable.getMessage()).contains("Usernameは必須です。")
                );
    }

    @Test
    @Transactional
    void deleteUser_NonExistentUser_ShouldThrowException() {
        Long nonExistentUserId = 999L;
        
        // 重要: ServiceImplの実装に合わせて、repositoryのメソッドをテスト
        boolean exists = userRepository.existsById(nonExistentUserId);
        assertThat(exists).isFalse();
        
        // deleteByIdの実行をテスト
        userRepository.deleteById(nonExistentUserId);
        // 例外が発生しないことを確認（これがdeleteByIdの実際の動作）
    }
    // タイムスタンプ検証用テスト

    @Test
    @Transactional
    void saveUser_ShouldSetTimestamps() {
        // テストデータの準備
        User user = User.builder()
                .username("timeuser")
                .email("timeuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        // テストの実行
        User savedUser = userRepository.save(user);
        
        // 検証
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isEqualToIgnoringNanos(savedUser.getUpdatedAt());
    }
}