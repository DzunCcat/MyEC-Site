package com.example.userservice.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.userservice.config.JpaConfig;
import com.example.userservice.entity.User;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
@Import(JpaConfig.class)  // この行を追加
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL_DOMAIN = "@test.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.flush();
        
        passwordEncoder = new BCryptPasswordEncoder();
    }

    // 正常系テスト群

    @Test
    @Transactional
    void saveUser_Success() {
        User user = User.builder()
                .username("testuser")
                .email("testuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isInstanceOf(UUID.class);
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser" + TEST_EMAIL_DOMAIN);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(BCrypt.checkpw(TEST_PASSWORD, savedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    void findByUsername_Success() {
        User user = User.builder()
                .username("finduser")
                .email("finduser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.save(user);

        User found = userRepository.findByUsername("finduser").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("finduser" + TEST_EMAIL_DOMAIN);
    }

    @Test
    @Transactional
    void findByUsername_ReturnEmpty_WhenUserNotFound() {
        User found = userRepository.findByUsername("unknown").orElse(null);
        assertThat(found).isNull();
    }

    @Test
    @Transactional
    void existsByUsername_ReturnTrue_WhenExists() {
        User user = User.builder()
                .username("existsuser")
                .email("existsuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("existsuser");
        assertThat(exists).isTrue();
    }

    @Test
    @Transactional
    void existsByUsername_ReturnFalse_WhenNotExists() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    @Transactional
    void deleteUser_Success() {
        User user = User.builder()
                .username("deleteuser")
                .email("deleteuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        User savedUser = userRepository.save(user);

        userRepository.delete(savedUser);
        userRepository.flush();

        boolean exists = userRepository.existsById(savedUser.getId());
        assertThat(exists).isFalse();
    }

    // 異常系テスト群

    @Test
    @Transactional
    void saveUser_DuplicateUsername_ShouldThrowException() {
        User user1 = User.builder()
                .username("duplicateUser")
                .email("user1" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.saveAndFlush(user1);

        User user2 = User.builder()
                .username("duplicateUser")
                .email("user2" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("CONSTRAINT_INDEX")
                .hasMessageContaining("USERNAME");
    }

    @Test
    @Transactional
    void saveUser_DuplicateEmail_ShouldThrowException() {
        User user1 = User.builder()
                .username("user1")
                .email("duplicate" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        userRepository.saveAndFlush(user1);

        User user2 = User.builder()
                .username("user2")
                .email("duplicate" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("CONSTRAINT_INDEX")
                .hasMessageContaining("EMAIL");
    }

    @Test
    @Transactional
    void saveUser_NullUsername_ShouldThrowException() {
        User user = User.builder()
                .username(null)
                .email("nullusername" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

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
        UUID nonExistentUserId = UUID.randomUUID();
        
        boolean exists = userRepository.existsById(nonExistentUserId);
        assertThat(exists).isFalse();
        
        userRepository.deleteById(nonExistentUserId);
    }

    @Test
    @Transactional
    void saveUser_ShouldSetTimestamps() {
        User user = User.builder()
                .username("timeuser")
                .email("timeuser" + TEST_EMAIL_DOMAIN)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .build();

        User savedUser = userRepository.saveAndFlush(user); 
        
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isEqualToIgnoringNanos(savedUser.getUpdatedAt());

        savedUser.setEmail("updated" + TEST_EMAIL_DOMAIN);
        User updatedUser = userRepository.saveAndFlush(savedUser);

        assertThat(updatedUser.getCreatedAt()).isEqualTo(savedUser.getCreatedAt());  
        assertThat(updatedUser.getUpdatedAt()).isAfter(savedUser.getCreatedAt());    
    }

}