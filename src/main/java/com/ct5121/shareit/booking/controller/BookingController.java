package com.ct5121.shareit.booking.controller;

import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.service.BookingService;
import com.ct5121.shareit.security.ShareItUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a booking")
    public ResponseEntity<BookingResponseDto> createBooking(
            @AuthenticationPrincipal @Parameter(hidden = true) ShareItUserDetails currentUser,
            @Valid @RequestBody @Parameter(description = "Booking data") BookingRequestDto bookingRequestDto) {
        return ResponseEntity.ok(bookingService.createBooking(currentUser.getId(), bookingRequestDto));
    }

    @PatchMapping("/{bookingId}")
    @Operation(summary = "Approve or reject booking")
    public ResponseEntity<BookingResponseDto> approveBooking(
            @AuthenticationPrincipal @Parameter(hidden = true) ShareItUserDetails currentUser,
            @PathVariable @Parameter(description = "Booking ID") Long bookingId,
            @RequestParam @Parameter(description = "Booking approval") boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(currentUser.getId(), bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingResponseDto> getBooking(
            @AuthenticationPrincipal @Parameter(hidden = true) ShareItUserDetails currentUser,
            @PathVariable @Parameter(description = "Booking ID") Long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(currentUser.getId(), bookingId));
    }

    @GetMapping
    @Operation(summary = "Get user bookings")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @AuthenticationPrincipal @Parameter(hidden = true) ShareItUserDetails currentUser,
            @RequestParam(defaultValue = "ALL") @Parameter(description = "Booking state") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Parameter(description = "Index of first element") int from,
            @RequestParam(defaultValue = "10") @Positive @Parameter(description = "Page size") int size) {
        return ResponseEntity.ok(bookingService.getUserBookings(currentUser.getId(), state, from, size));
    }

    @GetMapping("/owner")
    @Operation(summary = "Get bookings for owner's items")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @AuthenticationPrincipal @Parameter(hidden = true) ShareItUserDetails currentUser,
            @RequestParam(defaultValue = "ALL") @Parameter(description = "Booking state") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Parameter(description = "Index of first element") int from,
            @RequestParam(defaultValue = "10") @Positive @Parameter(description = "Page size") int size) {
        return ResponseEntity.ok(bookingService.getOwnerBookings(currentUser.getId(), state, from, size));
    }
}