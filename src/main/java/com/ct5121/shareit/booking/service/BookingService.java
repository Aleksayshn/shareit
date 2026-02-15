package com.ct5121.shareit.booking.service;


import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size);

    List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size);
}
