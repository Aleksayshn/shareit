package com.ct5121.shareit.item.service;

import com.ct5121.shareit.item.dto.CommentDto;
import com.ct5121.shareit.item.dto.CommentResponseDto;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto addItem(Long userId, ItemRequestDto itemRequestDto);

    ItemResponseDto updateItem(Long userId, Long itemId, ItemRequestDto itemRequestDto);

    ItemResponseDto getItem(Long userId, Long itemId);

    List<ItemResponseDto> getUserItems(Long userId);

    List<ItemResponseDto> searchItems(String text, int from, int size);

    CommentResponseDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
