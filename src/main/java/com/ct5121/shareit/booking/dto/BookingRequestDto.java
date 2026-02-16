package com.ct5121.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequestDto {
    @NotNull(message = "Item ID cannot be empty")
    private Long itemId;

    @NotNull(message = "Reservation start date cannot be empty")
    @Future(message = "Reservation start date must be in the future")
    private LocalDateTime start;

    @NotNull(message = "Reservation end date cannot be empty")
    @Future(message = "Reservation end date must be in the future")
    private LocalDateTime end;
}