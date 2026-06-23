package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            WHERE UPPER(b.bookingCode) = UPPER(:bookingCode)
              AND u.phone = :phone
            """)
    Optional<Bookings> findByBookingCodeAndUserPhoneWithDetails(
            @Param("bookingCode") String bookingCode,
            @Param("phone") String phone
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
