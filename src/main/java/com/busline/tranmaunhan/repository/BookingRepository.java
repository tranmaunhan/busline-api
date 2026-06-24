package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Bookings, Integer> {

    /**
     * Tìm booking theo id, đồng thời fetch tickets và thông tin liên quan
     * để tránh N+1 query.
     */
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
            WHERE UPPER(b.bookingCode) = UPPER(:bookingCode)
              AND u.phone = :phone
            """)
    Optional<Bookings> findByBookingCodeAndUserPhoneWithDetails(
            @Param("bookingCode") String bookingCode,
            @Param("phone") String phone
    );

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
}
