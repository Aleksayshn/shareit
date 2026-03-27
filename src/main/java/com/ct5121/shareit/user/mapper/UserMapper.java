package com.ct5121.shareit.user.mapper;

import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
}
