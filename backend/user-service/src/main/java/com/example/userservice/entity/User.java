package com.example.userservice.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.UuidGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * ユーザー情報を表すエンティティクラス。
 * BaseEntityを継承することで、作成日時と更新日時の自動管理機能を取り込んでいます。
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder  // BaseEntityのフィールドもビルダーで扱えるようにするため、@BuilderからSuperBuilderに変更
@Table(name = "users")
@ToString(exclude = "password")
public class User extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message="Usernameは必須です。")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message= "Emailは必須です。")
    @Email(message="有効なEmailを入力してください。")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message="Passwordは必須です。")
    @Size(min=8, message="Passwordを8文字以上で入力してください。")
    @Column(nullable = false)
    private String password;


    public void setPassword(String password) {
        this.password = password;
    }

}