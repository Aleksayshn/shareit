package com.ct5121.shareit.comment.mapper;

import com.ct5121.shareit.comment.dto.CommentResponseDto;
import com.ct5121.shareit.comment.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "author", source = "author")
    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getName())")
    CommentResponseDto toCommentResponseDto(Comment comment);
}
