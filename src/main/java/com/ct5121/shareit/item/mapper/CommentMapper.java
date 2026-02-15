package com.ct5121.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.ct5121.shareit.item.dto.CommentResponseDto;
import com.ct5121.shareit.item.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "author", source = "author")
    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getName())")
    CommentResponseDto toCommentResponseDto(Comment comment);
}