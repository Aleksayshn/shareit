package com.ct5121.shareit.booking.dto;

import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.item.dto.ItemResponesDto;
import com.ct5121.shareit.user.dto.UserResponesDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemResponesDto item;
    private UserResponesDto booker;
    private Booking.BookingStatus status;
}