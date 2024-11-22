package com.example.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.userservice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ユーザー名で検索
    Optional<User> findByUsername(String username);

    // メールアドレスで検索
    Optional<User> findByEmail(String email);

    // ユーザー名が存在するかどうか確認
    boolean existsByUsername(String username);

    // メールアドレスが存在するかどうか確認
    boolean existsByEmail(String email);
}
