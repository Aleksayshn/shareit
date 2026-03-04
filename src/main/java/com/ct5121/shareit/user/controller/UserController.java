package com.ct5121.shareit.user.controller;

import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;
import com.ct5121.shareit.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API for working with users of the rental service")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Add a new user",
            description = "Creates a new user in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User created"),
                    @ApiResponse(responseCode = "400", description = "Invalid user data")
            })
    public UserResponseDto addUser(
            @Valid @RequestBody
            @Parameter(description = "Data of the new user", required = true)
            UserRequestDto user) {
        return userService.addUser(user);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns information about a user by ID")
    public UserResponseDto getUser(
            @PathVariable
            @Parameter(description = "User ID", required = true, example = "1")
            Long userId) {
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update user data", description = "Updates existing user information")
    public UserResponseDto updateUser(
            @PathVariable
            @Parameter(description = "User ID to update", required = true, example = "1")
            Long userId,
            @Valid @RequestBody
            @Parameter(description = "Updated user data", required = true)
            UserUpdateDto user) {
        return userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID")
    public void deleteUser(
            @PathVariable
            @Parameter(description = "User ID to delete", required = true, example = "1")
            Long userId) {
        userService.deleteUser(userId);
    }
}
