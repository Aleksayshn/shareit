package com.ct5121.shareit.booking.dto;

import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.item.dto.ItemResponseDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemResponseDto item;
    private UserResponseDto booker;
    private Booking.BookingStatus status;
}
