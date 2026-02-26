package com.ct5121.shareit.item.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ct5121.shareit.item.dto.CommentDto;
import com.ct5121.shareit.item.dto.CommentResponseDto;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponesDto;
import com.ct5121.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Item Management", description = "API for working with items for rent")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @Operation(
            summary = "Add a new item",
            description = "Creates a new item available for rent by other users",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Item successfully added"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid item data"
                    )
            })
    public ResponseEntity<ItemResponesDto> addItem(
            @RequestHeader("X-Sharer-User-Id")
            @Parameter(description = "ID of the item owner", required = true, example = "1")
            Long userId,
            @Valid
            @RequestBody
            @Parameter(description = "Data of the new item", required = true)
            ItemRequestDto itemDto) {
        return ResponseEntity.ok(itemService.addItem(userId, itemDto));
    }

    @PatchMapping("/{itemId}")
    @Operation(
            summary = "Update an item",
            description = "Updates the data of an existing item. Only the owner can update the item",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Item successfully updated"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Item not found or user is not the owner"
                    )
            })
    public ResponseEntity<ItemResponesDto> updateItem(
            @RequestHeader("X-Sharer-User-Id")
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
    @Operation(
            summary = "Get item by ID",
            description = "Returns full information about an item by its ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Item information"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Item not found"
                    )
            })
    public ResponseEntity<ItemResponesDto> getItem(
            @RequestHeader("X-Sharer-User-Id")
            @Parameter(description = "User ID", required = true, example = "1")
            Long userId,
            @PathVariable
            @Parameter(description = "Item ID", required = true, example = "1")
            Long itemId) {
        return ResponseEntity.ok(itemService.getItem(userId, itemId));
    }

    @GetMapping
    @Operation(
            summary = "Get all items of a user",
            description = "Returns a list of all items belonging to a specified user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of user's items"
                    )
            })
    public ResponseEntity<List<ItemResponesDto>> getUserItems(
            @RequestHeader("X-Sharer-User-Id")
            @Parameter(description = "User ID", required = true, example = "1")
            Long userId) {
        return ResponseEntity.ok(itemService.getUserItems(userId));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search items",
            description = "Searches for items available for rent by name or description",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of found items"
                    )
            })
    public ResponseEntity<List<ItemResponesDto>> searchItems(
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
    @Operation(
            summary = "Add a comment to an item",
            description = "Adds a comment to an item. Only a user who rented the item can leave a comment",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment successfully added"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User has not rented the item"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Item or user not found"
                    )
            })

    public ResponseEntity<CommentResponseDto> addComment(
            @RequestHeader("X-Sharer-User-Id")
            @Parameter(description = "User ID")
            Long userId,
            @PathVariable
            @Parameter(description = "Item ID")
            Long itemId,
            @Valid
            @RequestBody
            @Parameter(description = "Comment data")
            CommentDto commentDto) {
        return ResponseEntity.ok(itemService.addComment(userId, itemId, commentDto));
    }
}