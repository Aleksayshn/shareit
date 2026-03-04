package com.ct5121.shareit.item.mapper;

import com.ct5121.shareit.item.dto.CommentResponseDto;
import com.ct5121.shareit.item.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "author", source = "author")
    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getName())")
    CommentResponseDto toCommentResponseDto(Comment comment);
}
