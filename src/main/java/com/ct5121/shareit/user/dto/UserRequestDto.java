package com.ct5121.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequestDto {
    @NotNull(message = "Username cannot be empty.")
    private String name;
    @NotNull(message = "Email cannot be empty.")
    @Email(message = "Invalid email address.")// username or login
    private String email;
}