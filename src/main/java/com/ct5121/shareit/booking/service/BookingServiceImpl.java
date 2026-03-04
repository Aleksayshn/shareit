package com.ct5121.shareit.booking.service;

import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.mapper.BookingMapper;
import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.booking.repository.BookingRepository;
import com.ct5121.shareit.exception.BadRequestException;
import com.ct5121.shareit.exception.ForbiddenException;
import com.ct5121.shareit.exception.NotFoundException;
import com.ct5121.shareit.item.model.Item;
import com.ct5121.shareit.item.repository.ItemRepository;
import com.ct5121.shareit.user.model.User;
import com.ct5121.shareit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = getExistingUser(userId);
        Item item = getExistingItem(bookingRequestDto.getItemId());

        if (!item.isAvailable()) {
            throw new BadRequestException("Item is not available for booking");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Owner cannot book own item");
        }

        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();
        if (!start.isBefore(end)) {
            throw new BadRequestException("Booking start must be before booking end");
        }

        if (bookingRepository.existsOverlappingApprovedBooking(item.getId(), start, end)) {
            throw new BadRequestException("Item is already booked for the selected period");
        }

        Booking booking = new Booking(
                null,
                start,
                end,
                item,
                booker,
                Booking.BookingStatus.WAITING);

        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = getExistingBookingWithDetails(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only item owner can approve booking");
        }
        if (booking.getStatus() != Booking.BookingStatus.WAITING) {
            throw new BadRequestException("Booking has already been processed");
        }

        booking.setStatus(approved ? Booking.BookingStatus.APPROVED : Booking.BookingStatus.REJECTED);
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = getExistingBookingWithDetails(bookingId);

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("User has no access to this booking");
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Pageable pageable = buildOffsetPageRequest(from, size);
        LocalDateTime now = LocalDateTime.now();
        BookingState bookingState = BookingState.from(state);

        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findCurrentBookingsByBookerId(userId, now, pageable);
            case PAST -> bookingRepository.findPastBookingsByBookerId(userId, now, pageable);
            case FUTURE -> bookingRepository.findFutureBookingsByBookerId(userId, now, pageable);
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.WAITING,
                    pageable);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.REJECTED,
                    pageable);
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Pageable pageable = buildOffsetPageRequest(from, size);
        LocalDateTime now = LocalDateTime.now();
        BookingState bookingState = BookingState.from(state);

        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findCurrentBookingsByOwnerId(userId, now, pageable);
            case PAST -> bookingRepository.findPastBookingsByOwnerId(userId, now, pageable);
            case FUTURE -> bookingRepository.findFutureBookingsByOwnerId(userId, now, pageable);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.WAITING,
                    pageable);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.REJECTED,
                    pageable);
            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }

    private User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    private Item getExistingItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found"));
    }

    private Booking getExistingBookingWithDetails(Long bookingId) {
        return bookingRepository.findDetailedById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id " + bookingId + " not found"));
    }

    private Pageable buildOffsetPageRequest(int from, int size) {
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be zero or positive");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }
        return PageRequest.of(from / size, size);
    }
}
