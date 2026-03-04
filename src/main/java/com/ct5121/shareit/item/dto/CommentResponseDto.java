package com.ct5121.shareit.item.dto;

import com.ct5121.shareit.user.dto.UserResponseDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private String text;
    private String authorName;
    private UserResponseDto author;
    private LocalDateTime created;
}
