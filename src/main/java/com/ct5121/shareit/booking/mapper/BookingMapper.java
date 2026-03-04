package com.ct5121.shareit.booking.mapper;

import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.item.mapper.ItemMapper;
import com.ct5121.shareit.user.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {
    BookingResponseDto toBookingResponseDto(Booking booking);
}
