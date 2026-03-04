package com.ct5121.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDto {
    private String name;

    @Email(message = "Invalid email address")
    private String email;
}
