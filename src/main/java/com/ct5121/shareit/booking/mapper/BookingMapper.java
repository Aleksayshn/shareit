package com.ct5121.shareit.booking.mapper;

import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "booker")
    BookingResponseDto toBookingResponseDto(Booking booking);
}
