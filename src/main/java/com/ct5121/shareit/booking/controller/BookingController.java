package com.ct5121.shareit.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "API for working with bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader("X-Sharer-User-Id") @Parameter(description = "User ID") Long userId,
            @Valid @RequestBody @Parameter(description = "Booking data") BookingRequestDto bookingRequestDto) {
        return ResponseEntity.ok(bookingService.createBooking(userId, bookingRequestDto));
    }

    @PatchMapping("/{bookingId}")
    @Operation(summary = "Approve or reject a booking")
    public ResponseEntity<BookingResponseDto> approveBooking(
            @RequestHeader("X-Sharer-User-Id") @Parameter(description = "User ID") Long userId,
            @PathVariable @Parameter(description = "Booking ID") Long bookingId,
            @RequestParam @Parameter(description = "Booking approval") boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(userId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking information")
    public ResponseEntity<BookingResponseDto> getBooking(
            @RequestHeader("X-Sharer-User-Id") @Parameter(description = "User ID") Long userId,
            @PathVariable @Parameter(description = "Booking ID") Long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    @Operation(summary = "Get list of user's bookings")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") @Parameter(description = "User ID") Long userId,
            @RequestParam(defaultValue = "ALL") @Parameter(description = "Booking status") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Parameter(description = "Index of the first element") int from,
            @RequestParam(defaultValue = "10") @Positive @Parameter(description = "Number of elements to display") int size) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, state, from, size));
    }

    @GetMapping("/owner")
    @Operation(summary = "Get list of bookings for user's items")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") @Parameter(description = "User ID") Long userId,
            @RequestParam(defaultValue = "ALL") @Parameter(description = "Booking status") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Parameter(description = "Index of the first element") int from,
            @RequestParam(defaultValue = "10") @Positive @Parameter(description = "Number of elements to display") int size) {
        return ResponseEntity.ok(bookingService.getOwnerBookings(userId, state, from, size));
    }
}