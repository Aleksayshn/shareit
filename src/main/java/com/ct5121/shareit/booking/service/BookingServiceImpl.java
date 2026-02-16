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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (!item.isAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd()) ||
                bookingRequestDto.getStart().equals(bookingRequestDto.getEnd())) {
            throw new BadRequestException("Некорректные даты бронирования");
        }
        if (!bookingRepository.findOverlappingBookings(item.getId(),
                bookingRequestDto.getStart(), bookingRequestDto.getEnd()).isEmpty()) {
            throw new BadRequestException("Вещь уже забронирована на указанные даты");
        }
        Booking booking = new Booking();
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.WAITING);

        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь не является владельцем вещи");
        }
        if (booking.getStatus() != Booking.BookingStatus.WAITING) {
            throw new ForbiddenException("Бронирование уже обработано");
        }
        booking.setStatus(approved ? Booking.BookingStatus.APPROVED : Booking.BookingStatus.REJECTED);
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не имеет доступа к бронированию");
        }
        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = bookingRepository.findCurrentBookingsByBookerId(userId, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findPastBookingsByBookerId(userId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureBookingsByBookerId(userId, now, pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, Booking.BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, Booking.BookingStatus.REJECTED, pageable);
                break;
            default:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = bookingRepository.findCurrentBookingsByOwnerId(userId, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findPastBookingsByOwnerId(userId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureBookingsByOwnerId(userId, now, pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, Booking.BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, Booking.BookingStatus.REJECTED, pageable);
                break;
            default:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }
}
