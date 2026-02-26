package com.ct5121.shareit.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponesDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;
import com.ct5121.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Tag(name = "User Management",
        description = "API for working with users of the rental service")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Add a new user",
            description = "Creates a new user in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User created"),
                    @ApiResponse(responseCode = "400", description = "Invalid user data")
            })
    public UserResponesDto addUser(
            @Valid
            @RequestBody
            @Parameter(description = "Data of the new user", required = true)
            final UserRequestDto user) {
        return userService.addUser(user);
    }

    @GetMapping
    @Operation(summary = "Get all users",
            description = "Returns a list of all registered users",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of users")
            })
    public List<UserResponesDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID",
            description = "Returns information about a user by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User information"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public UserResponesDto getUser(
            @PathVariable
            @Parameter(description = "User ID", required = true, example = "1")
            Long userId) {
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update user data",
            description = "Updates information of an existing user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User data updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid update data"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public UserResponesDto updateUser(
            @PathVariable
            @Parameter(description = "User ID to update", required = true, example = "1")
            Long userId,
            @Valid
            @RequestBody
            @Parameter(description = "Updated user data", required = true)
            final UserUpdateDto user) {
        return userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user",
            description = "Deletes a user from the system by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User deleted"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public void deleteUser(
            @PathVariable
            @Parameter(description = "User ID to delete", required = true, example = "1")
            Long userId) {
        userService.deleteUser(userId);
    }
}