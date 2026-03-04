package com.ct5121.shareit.booking.controller;

import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
@Tag(name = "Bookings", description = "API for working with bookings")
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a booking")
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader(USER_ID_HEADER) @Parameter(description = "User ID") Long userId,
            @Valid @RequestBody @Parameter(description = "Booking data") BookingRequestDto bookingRequestDto) {
        return ResponseEntity.ok(bookingService.createBooking(userId, bookingRequestDto));
    }

    @PatchMapping("/{bookingId}")
    @Operation(summary = "Approve or reject booking")
    public ResponseEntity<BookingResponseDto> approveBooking(
            @RequestHeader(USER_ID_HEADER) @Parameter(description = "User ID") Long userId,
            @PathVariable @Parameter(description = "Booking ID") Long bookingId,
            @RequestParam @Parameter(description = "Booking approval") boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(userId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingResponseDto> getBooking(
            @RequestHeader(USER_ID_HEADER) @Parameter(description = "User ID") Long userId,
            @PathVariable @Parameter(description = "Booking ID") Long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    @Operation(summary = "Get user bookings")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @RequestHeader(USER_ID_HEADER) @Parameter(description = "User ID") Long userId,
            @RequestParam(defaultValue = "ALL") @Parameter(description = "Booking state") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Parameter(description = "Index of first element") int from,
            @RequestParam(defaultValue = "10") @Positive @Parameter(description = "Page size") int size) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, state, from, size));
    }

    @GetMapping("/owner")
    @Operation(summary = "Get bookings for owner's items")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestHeader(USER_ID_HEADER) @Parameter(description = "User ID") Long userId,
            @RequestParam(defaultValue = "ALL") @Parameter(description = "Booking state") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Parameter(description = "Index of first element") int from,
            @RequestParam(defaultValue = "10") @Positive @Parameter(description = "Page size") int size) {
        return ResponseEntity.ok(bookingService.getOwnerBookings(userId, state, from, size));
    }
}
