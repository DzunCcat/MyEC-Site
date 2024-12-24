package com.example.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPAの監査（Auditing）機能を有効化するための設定クラス。
 * このクラスにより、エンティティの作成日時や更新日時が自動的に管理されます。
 */
@Configuration  // このクラスがSpring設定クラスであることを示します
@EnableJpaAuditing  // JPA Auditing機能を有効化します
public class JpaConfig {

}