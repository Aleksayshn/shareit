package com.ct5121.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ct5121.shareit.exception.NotFoundException;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponesDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;
import com.ct5121.shareit.user.mapper.UserMapper;
import com.ct5121.shareit.user.model.User;
import com.ct5121.shareit.user.repository.UserRepository;
import com.ct5121.shareit.user.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponesDto addUser(UserRequestDto user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists.");
        }
        User newUser = userMapper.toUser(user);
        return userMapper.toUserResponesDto(userRepository.save(newUser));
    }

    @Override
    public List<UserResponesDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponesDto)
                .toList();
    }

    @Override
    public UserResponesDto getUserById(Long id) {
        return userMapper.toUserResponesDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with email " + id + " not found")));
    }

    @Override
    @Transactional
    public UserResponesDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with email " + id + " not found"));
        if (userUpdateDto.getName() != null) {
            user.setName(userUpdateDto.getName());
        }
        if (userUpdateDto.getEmail() != null) {
            if (!userUpdateDto.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(userUpdateDto.getEmail())) {
                throw new IllegalArgumentException("User with email " + userUpdateDto.getEmail() + " already exists.");
            }
            user.setEmail(userUpdateDto.getEmail());
        }
        return userMapper.toUserResponesDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with email " + id + " not found");
        }
        userRepository.deleteById(id);
    }
}