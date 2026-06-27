package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.TripSeats;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TripSeatRepository extends JpaRepository<TripSeats, Integer> {

    @Query("""
            SELECT
                ts.id AS tripSeatId,
                st.id AS seatTemplateId,
                st.seatCode AS seatCode,
                st.rowIndex AS rowIndex,
                st.colIndex AS colIndex,
                st.deck AS deck,
                st.seatType AS seatType,
                ts.status AS status
            FROM TripSeats ts
            JOIN ts.seatTemplate st
            WHERE ts.trip.id = :tripId
            ORDER BY st.deck, st.rowIndex, st.colIndex, st.seatCode
            """)
    List<TripSeatMapProjection> findSeatMapByTripId(@Param("tripId") Integer tripId);

    @Query("""
            SELECT
                ts.id AS tripSeatId,
                st.id AS seatTemplateId,
                st.seatCode AS seatCode,
                st.rowIndex AS rowIndex,
                st.colIndex AS colIndex,
                st.deck AS deck,
                st.seatType AS seatType,
                ts.status AS status,
                b.id AS bookingId,
                b.bookingCode AS bookingCode,
                b.status AS bookingStatus,
                COALESCE(b.contactName, u.fullName) AS contactName,
                COALESCE(b.contactPhone, u.phone) AS contactPhone
            FROM TripSeats ts
            JOIN ts.seatTemplate st
            LEFT JOIN Tickets t ON t.tripSeat = ts
            LEFT JOIN t.booking b
            LEFT JOIN b.user u
            WHERE ts.trip.id = :tripId
            ORDER BY st.deck, st.rowIndex, st.colIndex, st.seatCode
            """)
    List<AdminTripSeatMapProjection> findAdminSeatMapByTripId(@Param("tripId") Integer tripId);

    /**
     * Lấy danh sách ghế theo id với PESSIMISTIC WRITE LOCK (SELECT FOR UPDATE).
     * Dùng khi đặt vé để đảm bảo không có race condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TripSeats ts JOIN FETCH ts.seatTemplate WHERE ts.id IN :ids")
    List<TripSeats> findByIdInWithLock(@Param("ids") List<Integer> ids);

    List<TripSeats> findByIdIn(List<Integer> ids);

    @Query("""
            SELECT
                ts.trip.id AS tripId,
                COUNT(ts.id) AS availableSeatCount
            FROM TripSeats ts
            WHERE ts.trip.id IN :tripIds
              AND ts.status = 0
            GROUP BY ts.trip.id
            """)
    List<TripAvailableSeatProjection> countAvailableSeatsByTripIds(@Param("tripIds") Collection<Integer> tripIds);

    interface TripSeatMapProjection {
        Integer getTripSeatId();

        Integer getSeatTemplateId();

        String getSeatCode();

        Integer getRowIndex();

        Integer getColIndex();

        String getDeck();

        String getSeatType();

        Integer getStatus();
    }

    interface TripAvailableSeatProjection {
        Integer getTripId();

        Long getAvailableSeatCount();
    }

    interface AdminTripSeatMapProjection {
        Integer getTripSeatId();

        Integer getSeatTemplateId();

        String getSeatCode();

        Integer getRowIndex();

        Integer getColIndex();

        String getDeck();

        String getSeatType();

        Integer getStatus();

        Integer getBookingId();

        String getBookingCode();

        Integer getBookingStatus();

        String getContactName();

        String getContactPhone();
    }
}
