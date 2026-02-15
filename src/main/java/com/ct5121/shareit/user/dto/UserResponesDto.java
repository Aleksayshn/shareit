package com.ct5121.shareit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
public class UserResponesDto {
    private Long id;
    private String name;
    private String email;
}
