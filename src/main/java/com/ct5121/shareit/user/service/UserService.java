package com.ct5121.shareit.user.service;

import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserResponseDto addUser(UserRequestDto user);

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long id);

    UserResponseDto updateUser(Long id, UserUpdateDto userRequest);

    void deleteUser(Long id);
}
