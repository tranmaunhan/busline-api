package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
