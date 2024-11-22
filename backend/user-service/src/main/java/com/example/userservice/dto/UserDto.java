package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;

    @NotBlank(message = "Usernameは必須です。")
    private String username;

    @NotBlank(message = "Emailは必須です。")
    @Email(message = "有効なEmailを入力してください。")
    private String email;
}
