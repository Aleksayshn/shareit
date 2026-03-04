package com.ct5121.shareit.item.controller;

import com.ct5121.shareit.item.dto.CommentDto;
import com.ct5121.shareit.item.dto.CommentResponseDto;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponseDto;
import com.ct5121.shareit.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Tag(name = "Item Management", description = "API for working with items for rent")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    @Operation(summary = "Add a new item", description = "Creates a new item")
    public ResponseEntity<ItemResponseDto> addItem(
            @RequestHeader(USER_ID_HEADER)
            @Parameter(description = "ID of the item owner", required = true, example = "1")
            Long userId,
            @Valid @RequestBody
            @Parameter(description = "Data of the new item", required = true)
            ItemRequestDto itemDto) {
        return ResponseEntity.ok(itemService.addItem(userId, itemDto));
    }

    @PatchMapping("/{itemId}")
    @Operation(summary = "Update an item", description = "Only owner can update item")
    public ResponseEntity<ItemResponseDto> updateItem(
            @RequestHeader(USER_ID_HEADER)
            @Parameter(description = "ID of the item owner", required = true, example = "1")
            Long userId,
            @PathVariable
            @Parameter(description = "ID of the item to update", required = true, example = "1")
            Long itemId,
            @RequestBody
            @Parameter(description = "Updated item data", required = true)
            ItemRequestDto itemDto) {
        return ResponseEntity.ok(itemService.updateItem(userId, itemId, itemDto));
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Get item by ID", description = "Returns item details by ID")
    public ResponseEntity<ItemResponseDto> getItem(
            @RequestHeader(USER_ID_HEADER)
            @Parameter(description = "User ID", required = true, example = "1")
            Long userId,
            @PathVariable
            @Parameter(description = "Item ID", required = true, example = "1")
            Long itemId) {
        return ResponseEntity.ok(itemService.getItem(userId, itemId));
    }

    @GetMapping
    @Operation(summary = "Get user items", description = "Returns all items belonging to user")
    public ResponseEntity<List<ItemResponseDto>> getUserItems(
            @RequestHeader(USER_ID_HEADER)
            @Parameter(description = "User ID", required = true, example = "1")
            Long userId) {
        return ResponseEntity.ok(itemService.getUserItems(userId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search items", description = "Searches available items by text")
    public ResponseEntity<List<ItemResponseDto>> searchItems(
            @RequestParam
            @Parameter(description = "Text to search", required = true, example = "drill")
            String text,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero
            @Parameter(description = "Index of the first element")
            int from,
            @RequestParam(defaultValue = "10")
            @Positive
            @Parameter(description = "Number of elements to display")
            int size) {
        return ResponseEntity.ok(itemService.searchItems(text, from, size));
    }

    @PostMapping("/{itemId}/comment")
    @Operation(summary = "Add comment", description = "Only users with completed booking can comment",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comment added"),
                    @ApiResponse(responseCode = "400", description = "Business rule violated")
            })
    public ResponseEntity<CommentResponseDto> addComment(
            @RequestHeader(USER_ID_HEADER)
            @Parameter(description = "User ID")
            Long userId,
            @PathVariable
            @Parameter(description = "Item ID")
            Long itemId,
            @Valid @RequestBody
            @Parameter(description = "Comment payload")
            CommentDto commentDto) {
        return ResponseEntity.ok(itemService.addComment(userId, itemId, commentDto));
    }
}
