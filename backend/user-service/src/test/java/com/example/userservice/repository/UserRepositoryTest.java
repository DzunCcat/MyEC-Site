// UserRepositoryTest.java
package com.example.userservice.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.example.userservice.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 実際のDBを使用
@TestPropertySource(locations = "classpath:application-test.yml")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveUser() {
        // Given
        User user = User.builder()
                .username("Bob")
                .email("bob@example.com")
                .password("password123")
                .build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("Bob", savedUser.getUsername());
    }

    @Test
    public void testFindById_UserExists() {
        // Given
        User user = User.builder()
                .username("Bob")
                .email("bob@example.com")
                .password("password123")
                .build();
        User savedUser = userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("Bob", foundUser.get().getUsername());
    }

    @Test
    public void testFindById_UserNotFound() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testDeleteUser() {
        // Given
        User user = User.builder()
                .username("Bob")
                .email("bob@example.com")
                .password("password123")
                .build();
        User savedUser = userRepository.save(user);

        // When
        userRepository.deleteById(savedUser.getId());
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertFalse(foundUser.isPresent());
    }
}
