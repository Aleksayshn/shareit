package com.ct5121.shareit.booking.repository;

import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.item.model.Item;
import com.ct5121.shareit.user.model.User;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {
    private final BookingRepository bookingRepository;
    private final TestEntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    BookingRepositoryTest(BookingRepository bookingRepository,
                          TestEntityManager entityManager,
                          EntityManagerFactory entityManagerFactory) {
        this.bookingRepository = bookingRepository;
        this.entityManager = entityManager;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Test
    void shouldLoadDetailedBookingWithAssociations() {
        User owner = persistUser("Owner", "booking-owner@example.com");
        User booker = persistUser("Booker", "booking-booker@example.com");
        Item item = persistItem(owner, "Projector");
        Booking booking = persistBooking(
                item,
                booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                Booking.BookingStatus.WAITING);

        entityManager.clear();

        Booking foundBooking = bookingRepository.findDetailedById(booking.getId()).orElseThrow();

        assertThat(foundBooking.getId()).isEqualTo(booking.getId());
        assertThat(Hibernate.isInitialized(foundBooking.getItem())).isTrue();
        assertThat(Hibernate.isInitialized(foundBooking.getBooker())).isTrue();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(foundBooking.getItem(), "owner")).isTrue();
        assertThat(foundBooking.getItem().getOwner().getEmail()).isEqualTo("booking-owner@example.com");
    }

    @Test
    void shouldUpdateAndDeleteBooking() {
        User owner = persistUser("Owner", "update-owner@example.com");
        User booker = persistUser("Booker", "update-booker@example.com");
        Item item = persistItem(owner, "Camera");
        Booking booking = persistBooking(
                item,
                booker,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                Booking.BookingStatus.WAITING);

        booking.setStatus(Booking.BookingStatus.APPROVED);
        bookingRepository.saveAndFlush(booking);
        bookingRepository.deleteById(booking.getId());
        bookingRepository.flush();

        assertThat(bookingRepository.findById(booking.getId())).isEmpty();
    }

    @Test
    void shouldDetectOverlappingApprovedBooking() {
        User owner = persistUser("Owner", "overlap-owner@example.com");
        User booker = persistUser("Booker", "overlap-booker@example.com");
        Item item = persistItem(owner, "Printer");
        persistBooking(
                item,
                booker,
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 3, 10, 0),
                Booking.BookingStatus.APPROVED);

        boolean hasOverlap = bookingRepository.existsOverlappingApprovedBooking(
                item.getId(),
                LocalDateTime.of(2026, 5, 2, 10, 0),
                LocalDateTime.of(2026, 5, 4, 10, 0));

        boolean hasNoOverlap = bookingRepository.existsOverlappingApprovedBooking(
                item.getId(),
                LocalDateTime.of(2026, 5, 4, 11, 0),
                LocalDateTime.of(2026, 5, 5, 11, 0));

        assertThat(hasOverlap).isTrue();
        assertThat(hasNoOverlap).isFalse();
    }

    @Test
    void shouldFindPastApprovedBookingsForItemsOrderedByItemAndEndDate() {
        User owner = persistUser("Owner", "past-owner@example.com");
        User firstBooker = persistUser("First Booker", "past-booker-one@example.com");
        User secondBooker = persistUser("Second Booker", "past-booker-two@example.com");
        Item firstItem = persistItem(owner, "Bike");
        Item secondItem = persistItem(owner, "Kayak");
        Booking laterFirstItemBooking = persistBooking(
                firstItem,
                firstBooker,
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 2, 9, 0),
                Booking.BookingStatus.APPROVED);
        Booking earlierFirstItemBooking = persistBooking(
                firstItem,
                secondBooker,
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 2, 9, 0),
                Booking.BookingStatus.APPROVED);
        Booking secondItemBooking = persistBooking(
                secondItem,
                secondBooker,
                LocalDateTime.of(2026, 1, 1, 9, 0),
                LocalDateTime.of(2026, 1, 2, 9, 0),
                Booking.BookingStatus.APPROVED);
        persistBooking(
                secondItem,
                secondBooker,
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 2, 9, 0),
                Booking.BookingStatus.REJECTED);

        entityManager.clear();

        List<Booking> bookings = bookingRepository.findPastApprovedBookingsForItems(
                List.of(firstItem.getId(), secondItem.getId()),
                LocalDateTime.of(2026, 4, 1, 0, 0));

        assertThat(bookings)
                .extracting(Booking::getId)
                .containsExactly(
                        laterFirstItemBooking.getId(),
                        earlierFirstItemBooking.getId(),
                        secondItemBooking.getId());
        assertThat(bookings).allSatisfy(booking -> {
            assertThat(Hibernate.isInitialized(booking.getItem())).isTrue();
            assertThat(Hibernate.isInitialized(booking.getBooker())).isTrue();
        });
    }

    @Test
    void shouldFindBookerBookingsOrderedByStartDesc() {
        User owner = persistUser("Owner", "booker-owner@example.com");
        User booker = persistUser("Booker", "booker-order@example.com");
        Item firstItem = persistItem(owner, "Microphone");
        Item secondItem = persistItem(owner, "Monitor");
        Booking earlierBooking = persistBooking(
                firstItem,
                booker,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                LocalDateTime.of(2026, 7, 2, 10, 0),
                Booking.BookingStatus.APPROVED);
        Booking laterBooking = persistBooking(
                secondItem,
                booker,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 2, 10, 0),
                Booking.BookingStatus.WAITING);

        entityManager.clear();

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(
                booker.getId(),
                PageRequest.of(0, 10));

        assertThat(bookings)
                .extracting(Booking::getId)
                .containsExactly(laterBooking.getId(), earlierBooking.getId());
    }

    private User persistUser(String name, String email) {
        return entityManager.persistAndFlush(new User(null, name, email, "password", LocalDateTime.now()));
    }

    private Item persistItem(User owner, String name) {
        return entityManager.persistAndFlush(new Item(null, name, name + " description", true, owner, null));
    }

    private Booking persistBooking(Item item,
                                   User booker,
                                   LocalDateTime start,
                                   LocalDateTime end,
                                   Booking.BookingStatus status) {
        return entityManager.persistAndFlush(new Booking(null, start, end, item, booker, status));
    }
}
