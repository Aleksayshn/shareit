package com.ct5121.shareit.booking.repository;

import com.ct5121.shareit.booking.model.Booking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    Optional<Booking> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Booking.BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.booker.id = :bookerId
              AND b.start <= :now
              AND b.end >= :now
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentBookingsByBookerId(@Param("bookerId") Long bookerId,
                                                @Param("now") LocalDateTime now,
                                                Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.booker.id = :bookerId
              AND b.end < :now
            ORDER BY b.start DESC
            """)
    List<Booking> findPastBookingsByBookerId(@Param("bookerId") Long bookerId,
                                             @Param("now") LocalDateTime now,
                                             Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.booker.id = :bookerId
              AND b.start > :now
            ORDER BY b.start DESC
            """)
    List<Booking> findFutureBookingsByBookerId(@Param("bookerId") Long bookerId,
                                               @Param("now") LocalDateTime now,
                                               Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId,
                                                             Booking.BookingStatus status,
                                                             Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.item.owner.id = :ownerId
              AND b.start <= :now
              AND b.end >= :now
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentBookingsByOwnerId(@Param("ownerId") Long ownerId,
                                               @Param("now") LocalDateTime now,
                                               Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.item.owner.id = :ownerId
              AND b.end < :now
            ORDER BY b.start DESC
            """)
    List<Booking> findPastBookingsByOwnerId(@Param("ownerId") Long ownerId,
                                            @Param("now") LocalDateTime now,
                                            Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.item.owner.id = :ownerId
              AND b.start > :now
            ORDER BY b.start DESC
            """)
    List<Booking> findFutureBookingsByOwnerId(@Param("ownerId") Long ownerId,
                                              @Param("now") LocalDateTime now,
                                              Pageable pageable);

    @Query("""
            SELECT (COUNT(b) > 0)
            FROM Booking b
            WHERE b.item.id = :itemId
              AND b.status = 'APPROVED'
              AND b.start <= :end
              AND b.end >= :start
            """)
    boolean existsOverlappingApprovedBooking(@Param("itemId") Long itemId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId,
                                                           Long itemId,
                                                           Booking.BookingStatus status,
                                                           LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item", "item.owner"})
    Optional<Booking> findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(Long itemId,
                                                                            Booking.BookingStatus status,
                                                                            LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item", "item.owner"})
    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId,
                                                                              Booking.BookingStatus status,
                                                                              LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item", "item.owner"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.item.id IN :itemIds
              AND b.status = 'APPROVED'
              AND b.end < :now
            ORDER BY b.item.id, b.end DESC
            """)
    List<Booking> findPastApprovedBookingsForItems(@Param("itemIds") Collection<Long> itemIds,
                                                   @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"booker", "item", "item.owner"})
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.item.id IN :itemIds
              AND b.status = 'APPROVED'
              AND b.start > :now
            ORDER BY b.item.id, b.start ASC
            """)
    List<Booking> findFutureApprovedBookingsForItems(@Param("itemIds") Collection<Long> itemIds,
                                                     @Param("now") LocalDateTime now);
}
