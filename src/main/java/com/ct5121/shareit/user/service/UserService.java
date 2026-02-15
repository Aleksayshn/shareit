package com.ct5121.shareit.user.service;

import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponesDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    UserResponesDto addUser(UserRequestDto user);

    List<UserResponesDto> getAllUsers();

    UserResponesDto getUserById(Long id);

    UserResponesDto updateUser(Long id, UserUpdateDto userRequest);

    void deleteUser(Long id);
}
