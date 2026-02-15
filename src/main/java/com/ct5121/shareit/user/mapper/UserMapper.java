package com.ct5121.shareit.user.mapper;

import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponesDto;
import com.ct5121.shareit.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRequestDto user);

    UserResponesDto toUserResponesDto(User user);
}