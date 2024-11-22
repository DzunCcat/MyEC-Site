package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CreateUserDto {
	@NotBlank(message = "Usernameは必須です。")
	private String username;

	@NotBlank(message = "Emailは必須です。")
	@Email(message = "有効なEmailを入力してください。")
	private String email;

	@NotBlank(message = "Passwordは必須です。")
	@Size(min = 8, message = "Passwordを8文字以上で入力してください。")
	private String password;
}