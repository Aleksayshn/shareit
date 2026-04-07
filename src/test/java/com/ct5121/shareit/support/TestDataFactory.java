package com.ct5121.shareit.support;

import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.comment.dto.CommentDto;
import com.ct5121.shareit.comment.dto.CommentResponseDto;
import com.ct5121.shareit.comment.model.Comment;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponseDto;
import com.ct5121.shareit.item.model.Item;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.dto.UserUpdateDto;
import com.ct5121.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static User user(Long id, String name, String email) {
        return new User(id, name, email, "encoded-password", LocalDateTime.now());
    }

    public static Item item(Long id, String name, boolean available, User owner) {
        return new Item(id, name, name + " description", available, owner, null);
    }

    public static Booking booking(Long id,
                                  LocalDateTime start,
                                  LocalDateTime end,
                                  Item item,
                                  User booker,
                                  Booking.BookingStatus status) {
        return new Booking(id, start, end, item, booker, status);
    }

    public static Comment comment(Long id, String text, Item item, User author, LocalDateTime created) {
        return new Comment(id, text, item, author, created);
    }

    public static UserRequestDto userRequestDto(String name, String email, String password) {
        UserRequestDto dto = new UserRequestDto();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    public static UserUpdateDto userUpdateDto(String name, String email) {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    public static UserResponseDto userResponseDto(Long id, String name, String email) {
        return new UserResponseDto(id, name, email);
    }

    public static ItemRequestDto itemRequestDto(String name, String description, Boolean available) {
        return new ItemRequestDto(name, description, available);
    }

    public static ItemResponseDto itemResponseDto(Long id, String name, boolean available, Long ownerId) {
        return new ItemResponseDto(
                id,
                name,
                name + " description",
                available,
                ownerId,
                null,
                null,
                null,
                List.of());
    }

    public static BookingRequestDto bookingRequestDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    public static BookingResponseDto bookingResponseDto(Long id,
                                                        LocalDateTime start,
                                                        LocalDateTime end,
                                                        Booking.BookingStatus status) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(id);
        dto.setStart(start);
        dto.setEnd(end);
        dto.setStatus(status);
        return dto;
    }

    public static CommentDto commentDto(String text) {
        CommentDto dto = new CommentDto();
        dto.setText(text);
        return dto;
    }

    public static CommentResponseDto commentResponseDto(Long id,
                                                        String text,
                                                        String authorName,
                                                        LocalDateTime created) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(id);
        dto.setText(text);
        dto.setAuthorName(authorName);
        dto.setCreated(created);
        return dto;
    }
}
