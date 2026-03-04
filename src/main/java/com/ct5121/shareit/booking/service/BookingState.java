package com.ct5121.shareit.booking.service;

import com.ct5121.shareit.exception.BadRequestException;

import java.util.Arrays;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String rawState) {
        return Arrays.stream(values())
                .filter(state -> state.name().equalsIgnoreCase(rawState))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown booking state: " + rawState));
    }
}
