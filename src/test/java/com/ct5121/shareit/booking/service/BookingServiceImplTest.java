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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ct5121.shareit.support.TestDataFactory.booking;
import static com.ct5121.shareit.support.TestDataFactory.bookingRequestDto;
import static com.ct5121.shareit.support.TestDataFactory.bookingResponseDto;
import static com.ct5121.shareit.support.TestDataFactory.item;
import static com.ct5121.shareit.support.TestDataFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository, bookingMapper);
    }

    @Test
    void shouldCreateBooking() {
        User owner = user(2L, "Owner", "owner@example.com");
        User booker = user(1L, "Booker", "booker@example.com");
        Item item = item(10L, "Drill", true, owner);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto request = bookingRequestDto(10L, start, end);
        Booking savedBooking = booking(100L, start, end, item, booker, Booking.BookingStatus.WAITING);
        BookingResponseDto response = bookingResponseDto(100L, start, end, Booking.BookingStatus.WAITING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingApprovedBooking(10L, start, end)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingMapper.toBookingResponseDto(savedBooking)).thenReturn(response);

        BookingResponseDto actualResponse = bookingService.createBooking(1L, request);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking persistedBooking = bookingCaptor.getValue();

        assertThat(actualResponse).isEqualTo(response);
        assertThat(persistedBooking.getBooker()).isEqualTo(booker);
        assertThat(persistedBooking.getItem()).isEqualTo(item);
        assertThat(persistedBooking.getStart()).isEqualTo(start);
        assertThat(persistedBooking.getEnd()).isEqualTo(end);
        assertThat(persistedBooking.getStatus()).isEqualTo(Booking.BookingStatus.WAITING);
    }

    @Test
    void shouldThrowWhenCreatingBookingForMissingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(
                1L,
                bookingRequestDto(10L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 1 not found");

        verifyNoInteractions(itemRepository, bookingRepository, bookingMapper);
    }

    @Test
    void shouldThrowWhenCreatingBookingForMissingItem() {
        User booker = user(1L, "Booker", "booker@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(
                1L,
                bookingRequestDto(99L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Item with id 99 not found");
    }

    @Test
    void shouldThrowWhenCreatingBookingForUnavailableItem() {
        User owner = user(2L, "Owner", "owner@example.com");
        User booker = user(1L, "Booker", "booker@example.com");
        Item item = item(10L, "Drill", false, owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(
                1L,
                bookingRequestDto(10L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Item is not available for booking");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldThrowWhenOwnerBooksOwnItem() {
        User owner = user(1L, "Owner", "owner@example.com");
        Item item = item(10L, "Drill", true, owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(
                1L,
                bookingRequestDto(10L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Owner cannot book own item");
    }

    @Test
    void shouldThrowWhenBookingDatesAreInvalid() {
        User owner = user(2L, "Owner", "owner@example.com");
        User booker = user(1L, "Booker", "booker@example.com");
        Item item = item(10L, "Drill", true, owner);
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(1L, bookingRequestDto(10L, start, end)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Booking start must be before booking end");
    }

    @Test
    void shouldThrowWhenApprovedBookingOverlaps() {
        User owner = user(2L, "Owner", "owner@example.com");
        User booker = user(1L, "Booker", "booker@example.com");
        Item item = item(10L, "Drill", true, owner);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingApprovedBooking(10L, start, end)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(1L, bookingRequestDto(10L, start, end)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Item is already booked for the selected period");
    }

    @Test
    void shouldApproveBookingWhenOwnerApproves() {
        Booking booking = waitingBooking(100L, 2L, 1L);
        BookingResponseDto response = bookingResponseDto(
                100L,
                booking.getStart(),
                booking.getEnd(),
                Booking.BookingStatus.APPROVED);

        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(response);

        BookingResponseDto actualResponse = bookingService.approveBooking(2L, 100L, true);

        assertThat(actualResponse).isEqualTo(response);
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.APPROVED);
    }

    @Test
    void shouldRejectBookingWhenOwnerRejects() {
        Booking booking = waitingBooking(100L, 2L, 1L);
        BookingResponseDto response = bookingResponseDto(
                100L,
                booking.getStart(),
                booking.getEnd(),
                Booking.BookingStatus.REJECTED);

        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(response);

        BookingResponseDto actualResponse = bookingService.approveBooking(2L, 100L, false);

        assertThat(actualResponse).isEqualTo(response);
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.REJECTED);
    }

    @Test
    void shouldThrowWhenApprovingBookingByNonOwner() {
        Booking booking = waitingBooking(100L, 2L, 1L);

        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(99L, 100L, true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only item owner can approve booking");
    }

    @Test
    void shouldThrowWhenApprovingProcessedBooking() {
        Booking booking = booking(
                100L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item(10L, "Drill", true, user(2L, "Owner", "owner@example.com")),
                user(1L, "Booker", "booker@example.com"),
                Booking.BookingStatus.APPROVED);

        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(2L, 100L, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Booking has already been processed");
    }

    @Test
    void shouldThrowWhenApprovingMissingBooking() {
        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approveBooking(2L, 100L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Booking with id 100 not found");
    }

    @Test
    void shouldReturnBookingForBooker() {
        Booking booking = waitingBooking(100L, 2L, 1L);
        BookingResponseDto response = bookingResponseDto(
                100L,
                booking.getStart(),
                booking.getEnd(),
                Booking.BookingStatus.WAITING);

        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(response);

        BookingResponseDto actualResponse = bookingService.getBooking(1L, 100L);

        assertThat(actualResponse).isEqualTo(response);
    }

    @Test
    void shouldReturnBookingForOwner() {
        Booking booking = waitingBooking(100L, 2L, 1L);
        BookingResponseDto response = bookingResponseDto(
                100L,
                booking.getStart(),
                booking.getEnd(),
                Booking.BookingStatus.WAITING);

        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(response);

        BookingResponseDto actualResponse = bookingService.getBooking(2L, 100L);

        assertThat(actualResponse).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenGettingBookingWithoutAccess() {
        Booking booking = waitingBooking(100L, 2L, 1L);

        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBooking(99L, 100L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User has no access to this booking");

        verifyNoInteractions(bookingMapper);
    }

    @Test
    void shouldThrowWhenGettingMissingBooking() {
        when(bookingRepository.findDetailedById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(1L, 100L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Booking with id 100 not found");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"})
    void shouldReturnUserBookingsForRequestedState(String state) {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(2, 10);
        Booking booking = bookingForUserState(state, userId);
        BookingResponseDto response = bookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus());

        when(userRepository.existsById(userId)).thenReturn(true);
        stubUserBookings(state, userId, pageable, booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(response);

        List<BookingResponseDto> bookings = bookingService.getUserBookings(userId, state, 20, 10);

        assertThat(bookings).containsExactly(response);
        verifyUserBookingsQuery(state, userId, pageable);
    }

    @Test
    void shouldReturnEmptyUserBookingsWhenRepositoryReturnsNoMatches() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L, pageable)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingService.getUserBookings(1L, "ALL", 0, 10);

        assertThat(bookings).isEmpty();
        verifyNoInteractions(bookingMapper);
    }

    @Test
    void shouldThrowWhenGettingUserBookingsForUnknownState() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.getUserBookings(1L, "UNKNOWN", 0, 10))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unknown booking state: UNKNOWN");
    }

    @Test
    void shouldThrowWhenGettingUserBookingsForMissingUser() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getUserBookings(1L, "ALL", 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 1 not found");
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10, Parameter 'from' must be zero or positive",
            "0, 0, Parameter 'size' must be positive"
    })
    void shouldThrowWhenUserBookingPaginationIsInvalid(int from, int size, String message) {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.getUserBookings(1L, "ALL", from, size))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"})
    void shouldReturnOwnerBookingsForRequestedState(String state) {
        Long ownerId = 2L;
        Pageable pageable = PageRequest.of(1, 5);
        Booking booking = bookingForOwnerState(state, ownerId);
        BookingResponseDto response = bookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus());

        when(userRepository.existsById(ownerId)).thenReturn(true);
        stubOwnerBookings(state, ownerId, pageable, booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(response);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId, state, 5, 5);

        assertThat(bookings).containsExactly(response);
        verifyOwnerBookingsQuery(state, ownerId, pageable);
    }

    @Test
    void shouldThrowWhenGettingOwnerBookingsForMissingUser() {
        when(userRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getOwnerBookings(2L, "ALL", 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 2 not found");
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10, Parameter 'from' must be zero or positive",
            "0, 0, Parameter 'size' must be positive"
    })
    void shouldThrowWhenOwnerBookingPaginationIsInvalid(int from, int size, String message) {
        when(userRepository.existsById(2L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.getOwnerBookings(2L, "ALL", from, size))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(message);
    }

    private Booking waitingBooking(Long bookingId, Long ownerId, Long bookerId) {
        User owner = user(ownerId, "Owner", "owner@example.com");
        User booker = user(bookerId, "Booker", "booker@example.com");
        Item item = item(10L, "Drill", true, owner);
        return booking(
                bookingId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                Booking.BookingStatus.WAITING);
    }

    private Booking bookingForUserState(String state, Long userId) {
        User owner = user(2L, "Owner", "owner@example.com");
        User booker = user(userId, "Booker", "booker@example.com");
        Booking.BookingStatus status = "REJECTED".equals(state)
                ? Booking.BookingStatus.REJECTED
                : "WAITING".equals(state)
                ? Booking.BookingStatus.WAITING
                : Booking.BookingStatus.APPROVED;
        return booking(
                100L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item(10L, "Drill", true, owner),
                booker,
                status);
    }

    private Booking bookingForOwnerState(String state, Long ownerId) {
        User owner = user(ownerId, "Owner", "owner@example.com");
        User booker = user(1L, "Booker", "booker@example.com");
        Booking.BookingStatus status = "REJECTED".equals(state)
                ? Booking.BookingStatus.REJECTED
                : "WAITING".equals(state)
                ? Booking.BookingStatus.WAITING
                : Booking.BookingStatus.APPROVED;
        return booking(
                200L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item(10L, "Drill", true, owner),
                booker,
                status);
    }

    private void stubUserBookings(String state, Long userId, Pageable pageable, Booking booking) {
        switch (BookingState.from(state)) {
            case CURRENT -> when(bookingRepository.findCurrentBookingsByBookerId(
                    eq(userId),
                    any(LocalDateTime.class),
                    eq(pageable)))
                    .thenReturn(List.of(booking));
            case PAST -> when(bookingRepository.findPastBookingsByBookerId(
                    eq(userId),
                    any(LocalDateTime.class),
                    eq(pageable)))
                    .thenReturn(List.of(booking));
            case FUTURE -> when(bookingRepository.findFutureBookingsByBookerId(
                    eq(userId),
                    any(LocalDateTime.class),
                    eq(pageable)))
                    .thenReturn(List.of(booking));
            case WAITING -> when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.WAITING,
                    pageable))
                    .thenReturn(List.of(booking));
            case REJECTED -> when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.REJECTED,
                    pageable))
                    .thenReturn(List.of(booking));
            case ALL -> when(bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable))
                    .thenReturn(List.of(booking));
        }
    }

    private void verifyUserBookingsQuery(String state, Long userId, Pageable pageable) {
        switch (BookingState.from(state)) {
            case CURRENT -> verify(bookingRepository).findCurrentBookingsByBookerId(
                    eq(userId),
                    any(LocalDateTime.class),
                    eq(pageable));
            case PAST -> verify(bookingRepository).findPastBookingsByBookerId(
                    eq(userId),
                    any(LocalDateTime.class),
                    eq(pageable));
            case FUTURE -> verify(bookingRepository).findFutureBookingsByBookerId(
                    eq(userId),
                    any(LocalDateTime.class),
                    eq(pageable));
            case WAITING -> verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.WAITING,
                    pageable);
            case REJECTED -> verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    Booking.BookingStatus.REJECTED,
                    pageable);
            case ALL -> verify(bookingRepository).findByBookerIdOrderByStartDesc(userId, pageable);
        }
    }

    private void stubOwnerBookings(String state, Long ownerId, Pageable pageable, Booking booking) {
        switch (BookingState.from(state)) {
            case CURRENT -> when(bookingRepository.findCurrentBookingsByOwnerId(
                    eq(ownerId),
                    any(LocalDateTime.class),
                    eq(pageable)))
                    .thenReturn(List.of(booking));
            case PAST -> when(bookingRepository.findPastBookingsByOwnerId(
                    eq(ownerId),
                    any(LocalDateTime.class),
                    eq(pageable)))
                    .thenReturn(List.of(booking));
            case FUTURE -> when(bookingRepository.findFutureBookingsByOwnerId(
                    eq(ownerId),
                    any(LocalDateTime.class),
                    eq(pageable)))
                    .thenReturn(List.of(booking));
            case WAITING -> when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId,
                    Booking.BookingStatus.WAITING,
                    pageable))
                    .thenReturn(List.of(booking));
            case REJECTED -> when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId,
                    Booking.BookingStatus.REJECTED,
                    pageable))
                    .thenReturn(List.of(booking));
            case ALL -> when(bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable))
                    .thenReturn(List.of(booking));
        }
    }

    private void verifyOwnerBookingsQuery(String state, Long ownerId, Pageable pageable) {
        switch (BookingState.from(state)) {
            case CURRENT -> verify(bookingRepository).findCurrentBookingsByOwnerId(
                    eq(ownerId),
                    any(LocalDateTime.class),
                    eq(pageable));
            case PAST -> verify(bookingRepository).findPastBookingsByOwnerId(
                    eq(ownerId),
                    any(LocalDateTime.class),
                    eq(pageable));
            case FUTURE -> verify(bookingRepository).findFutureBookingsByOwnerId(
                    eq(ownerId),
                    any(LocalDateTime.class),
                    eq(pageable));
            case WAITING -> verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId,
                    Booking.BookingStatus.WAITING,
                    pageable);
            case REJECTED -> verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId,
                    Booking.BookingStatus.REJECTED,
                    pageable);
            case ALL -> verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
        }
    }
}
