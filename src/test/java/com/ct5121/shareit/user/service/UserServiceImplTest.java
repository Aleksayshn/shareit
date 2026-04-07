package com.ct5121.shareit.user.service;

import com.ct5121.shareit.exception.NotFoundException;
import com.ct5121.shareit.exception.UserAlreadyExistsException;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;
import com.ct5121.shareit.user.mapper.UserMapper;
import com.ct5121.shareit.user.model.User;
import com.ct5121.shareit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.ct5121.shareit.support.TestDataFactory.user;
import static com.ct5121.shareit.support.TestDataFactory.userRequestDto;
import static com.ct5121.shareit.support.TestDataFactory.userResponseDto;
import static com.ct5121.shareit.support.TestDataFactory.userUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void shouldAddUserAndEncodePassword() {
        UserRequestDto request = userRequestDto("Alice", "alice@example.com", "password123");
        User savedUser = user(1L, "Alice", "alice@example.com");
        UserResponseDto response = userResponseDto(1L, "Alice", "alice@example.com");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponseDto(savedUser)).thenReturn(response);

        UserResponseDto actualResponse = userService.addUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();

        assertThat(actualResponse).isEqualTo(response);
        assertThat(persistedUser.getId()).isNull();
        assertThat(persistedUser.getName()).isEqualTo("Alice");
        assertThat(persistedUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(persistedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(persistedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldThrowWhenAddingUserWithDuplicateEmail() {
        UserRequestDto request = userRequestDto("Alice", "alice@example.com", "password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.addUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email alice@example.com already exists");

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldGetAllUsers() {
        User firstUser = user(1L, "Alice", "alice@example.com");
        User secondUser = user(2L, "Bob", "bob@example.com");
        UserResponseDto firstResponse = userResponseDto(1L, "Alice", "alice@example.com");
        UserResponseDto secondResponse = userResponseDto(2L, "Bob", "bob@example.com");

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));
        when(userMapper.toUserResponseDto(firstUser)).thenReturn(firstResponse);
        when(userMapper.toUserResponseDto(secondUser)).thenReturn(secondResponse);

        List<UserResponseDto> users = userService.getAllUsers();

        assertThat(users).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void shouldGetUserById() {
        User existingUser = user(1L, "Alice", "alice@example.com");
        UserResponseDto response = userResponseDto(1L, "Alice", "alice@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userMapper.toUserResponseDto(existingUser)).thenReturn(response);

        UserResponseDto userResponse = userService.getUserById(1L);

        assertThat(userResponse).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenGettingUserByIdAndUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 99 not found");

        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldUpdateUserNameAndEmail() {
        User existingUser = user(1L, "Alice", "alice@example.com");
        UserUpdateDto request = userUpdateDto("Alice Updated", "updated@example.com");
        UserResponseDto response = userResponseDto(1L, "Alice Updated", "updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toUserResponseDto(existingUser)).thenReturn(response);

        UserResponseDto actualResponse = userService.updateUser(1L, request);

        assertThat(actualResponse).isEqualTo(response);
        assertThat(existingUser.getName()).isEqualTo("Alice Updated");
        assertThat(existingUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void shouldUpdateOnlyUserNameWhenEmailIsNotProvided() {
        User existingUser = user(1L, "Alice", "alice@example.com");
        UserUpdateDto request = userUpdateDto("Alice Updated", null);
        UserResponseDto response = userResponseDto(1L, "Alice Updated", "alice@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toUserResponseDto(existingUser)).thenReturn(response);

        UserResponseDto actualResponse = userService.updateUser(1L, request);

        assertThat(actualResponse).isEqualTo(response);
        assertThat(existingUser.getName()).isEqualTo("Alice Updated");
        assertThat(existingUser.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository, never()).existsByEmailAndIdNot(any(), any());
    }

    @Test
    void shouldThrowWhenUpdatingUserWithDuplicateEmail() {
        User existingUser = user(1L, "Alice", "alice@example.com");
        UserUpdateDto request = userUpdateDto("Alice Updated", "bob@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("bob@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email bob@example.com already exists");
    }

    @Test
    void shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingMissingUser() {
        when(userRepository.existsById(77L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(77L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 77 not found");
    }
}
