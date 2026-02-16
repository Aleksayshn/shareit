package com.ct5121.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class ItemRequestDto {
    @NotBlank(message = "The item name cannot be empty.")
    private String name;
    @NotBlank(message = "The item description cannot be empty.")
    private String description;
    @NotNull(message = "The item owner cannot be empty.")
    private Boolean available;
}
