package com.ct5121.shareit.user.service;

import com.ct5121.shareit.exception.BadRequestException;
import com.ct5121.shareit.exception.NotFoundException;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;
import com.ct5121.shareit.user.mapper.UserMapper;
import com.ct5121.shareit.user.model.User;
import com.ct5121.shareit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto addUser(UserRequestDto user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("User with email " + user.getEmail() + " already exists");
        }
        User newUser = userMapper.toUser(user);
        return userMapper.toUserResponseDto(userRepository.save(newUser));
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return userMapper.toUserResponseDto(getExistingUser(id));
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = getExistingUser(id);

        if (userUpdateDto.getName() != null) {
            user.setName(userUpdateDto.getName());
        }

        if (userUpdateDto.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(userUpdateDto.getEmail(), id)) {
                throw new BadRequestException("User with email " + userUpdateDto.getEmail() + " already exists");
            }
            user.setEmail(userUpdateDto.getEmail());
        }

        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    private User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }
}
