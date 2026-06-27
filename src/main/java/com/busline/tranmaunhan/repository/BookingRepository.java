package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Bookings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Bookings, Integer> {

    @Query("""
            SELECT b FROM Bookings b
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.tripSeat ts
            LEFT JOIN FETCH ts.seatTemplate
            WHERE b.id = :id
            """)
    Optional<Bookings> findByIdWithTickets(@Param("id") Integer id);

    @Query("""
            SELECT DISTINCT b FROM Bookings b
            JOIN b.user u
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.trip trip
            LEFT JOIN FETCH trip.route route
            LEFT JOIN FETCH route.origin
            LEFT JOIN FETCH route.destination
            LEFT JOIN FETCH t.tripSeat ts
            LEFT JOIN FETCH ts.seatTemplate
            LEFT JOIN FETCH t.pickupStop pickupStop
            LEFT JOIN FETCH pickupStop.location
            LEFT JOIN FETCH t.dropoffStop dropoffStop
            LEFT JOIN FETCH dropoffStop.location
            WHERE b.id = :bookingId
              AND u.id = :userId
            """)
    Optional<Bookings> findByIdAndUserIdWithDetails(
            @Param("bookingId") Integer bookingId,
            @Param("userId") Integer userId
    );

    @Query("""
            SELECT DISTINCT b FROM Bookings b
            LEFT JOIN FETCH b.user u
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.trip trip
            LEFT JOIN FETCH trip.route route
            LEFT JOIN FETCH route.origin
            LEFT JOIN FETCH route.destination
            LEFT JOIN FETCH t.tripSeat ts
            LEFT JOIN FETCH ts.seatTemplate
            LEFT JOIN FETCH t.pickupStop pickupStop
            LEFT JOIN FETCH pickupStop.location
            LEFT JOIN FETCH t.dropoffStop dropoffStop
            LEFT JOIN FETCH dropoffStop.location
            WHERE b.id = :bookingId
            """)
    Optional<Bookings> findByIdWithDetails(@Param("bookingId") Integer bookingId);

    @Query("""
            SELECT DISTINCT b FROM Bookings b
            LEFT JOIN FETCH b.user u
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.trip trip
            LEFT JOIN FETCH trip.route route
            LEFT JOIN FETCH route.origin
            LEFT JOIN FETCH route.destination
            LEFT JOIN FETCH t.tripSeat ts
            LEFT JOIN FETCH ts.seatTemplate
            LEFT JOIN FETCH t.pickupStop pickupStop
            LEFT JOIN FETCH pickupStop.location
            LEFT JOIN FETCH t.dropoffStop dropoffStop
            LEFT JOIN FETCH dropoffStop.location
            WHERE UPPER(b.bookingCode) = UPPER(:bookingCode)
              AND b.contactPhone = :phone
            """)
    Optional<Bookings> findByBookingCodeAndContactPhoneWithDetails(
            @Param("bookingCode") String bookingCode,
            @Param("phone") String phone
    );

    @Query("""
            SELECT DISTINCT b FROM Bookings b
            LEFT JOIN FETCH b.user u
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.trip trip
            LEFT JOIN FETCH trip.route route
            LEFT JOIN FETCH route.origin
            LEFT JOIN FETCH route.destination
            LEFT JOIN FETCH t.tripSeat ts
            LEFT JOIN FETCH ts.seatTemplate
            LEFT JOIN FETCH t.pickupStop pickupStop
            LEFT JOIN FETCH pickupStop.location
            LEFT JOIN FETCH t.dropoffStop dropoffStop
            LEFT JOIN FETCH dropoffStop.location
            WHERE UPPER(b.bookingCode) = UPPER(:bookingCode)
            """)
    Optional<Bookings> findByBookingCodeWithDetails(@Param("bookingCode") String bookingCode);

    @Query("""
            SELECT b.status
            FROM Bookings b
            WHERE UPPER(b.bookingCode) = UPPER(:bookingCode)
            """)
    Optional<Integer> findStatusByBookingCode(@Param("bookingCode") String bookingCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Bookings b
            SET b.status = :paidStatus
            WHERE UPPER(b.bookingCode) = UPPER(:bookingCode)
              AND b.status = :pendingStatus
              AND b.totalAmount = :transferAmount
              AND (b.paymentExpiry IS NULL OR b.paymentExpiry > CURRENT_TIMESTAMP)
            """)
    int markAsPaidByBookingCodeIfPending(
            @Param("bookingCode") String bookingCode,
            @Param("pendingStatus") Integer pendingStatus,
            @Param("paidStatus") Integer paidStatus,
            @Param("transferAmount") BigDecimal transferAmount
    );

    @Query("""
            SELECT DISTINCT b FROM Bookings b
            JOIN b.user u
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.trip trip
            LEFT JOIN FETCH trip.route route
            LEFT JOIN FETCH route.origin
            LEFT JOIN FETCH route.destination
            LEFT JOIN FETCH t.tripSeat ts
            LEFT JOIN FETCH ts.seatTemplate
            LEFT JOIN FETCH t.pickupStop pickupStop
            LEFT JOIN FETCH pickupStop.location
            LEFT JOIN FETCH t.dropoffStop dropoffStop
            LEFT JOIN FETCH dropoffStop.location
            WHERE u.id = :userId
            ORDER BY b.bookingTime DESC, b.id DESC
            """)
    List<Bookings> findByUserIdWithDetails(@Param("userId") Integer userId);

    @Query("""
            SELECT b.id
            FROM Bookings b
            ORDER BY b.bookingTime DESC, b.id DESC
            """)
    List<Integer> findLatestBookingIds(Pageable pageable);

    @Query("""
            SELECT DISTINCT b FROM Bookings b
            LEFT JOIN FETCH b.user u
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.trip trip
            LEFT JOIN FETCH trip.route route
            LEFT JOIN FETCH route.origin
            LEFT JOIN FETCH route.destination
            LEFT JOIN FETCH t.tripSeat ts
            LEFT JOIN FETCH ts.seatTemplate
            WHERE b.id IN :bookingIds
            """)
    List<Bookings> findByIdInWithAdminDetails(@Param("bookingIds") Collection<Integer> bookingIds);

    @Query("""
            SELECT DISTINCT b FROM Bookings b
            LEFT JOIN FETCH b.user u
            LEFT JOIN FETCH b.tickets t
            LEFT JOIN FETCH t.tripSeat ts
            WHERE b.status = :pendingStatus
              AND b.paymentExpiry IS NOT NULL
              AND b.paymentExpiry <= :referenceTime
            """)
    List<Bookings> findExpiredPendingBookingsWithDetails(
            @Param("pendingStatus") Integer pendingStatus,
            @Param("referenceTime") OffsetDateTime referenceTime
    );

    @Query("""
            SELECT
                b.id AS bookingId,
                b.bookingTime AS bookingTime,
                b.status AS status,
                b.totalAmount AS totalAmount
            FROM Bookings b
            WHERE b.bookingTime >= :start
              AND b.bookingTime < :end
            """)
    List<AdminBookingMetricProjection> findAdminMetricsByBookingTimeBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    interface AdminBookingMetricProjection {
        Integer getBookingId();

        OffsetDateTime getBookingTime();

        Integer getStatus();

        BigDecimal getTotalAmount();
    }
}
