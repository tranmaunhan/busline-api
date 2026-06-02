package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.TripSeats;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Lấy danh sách ghế theo id với PESSIMISTIC WRITE LOCK (SELECT FOR UPDATE).
     * Dùng khi đặt vé để đảm bảo không có race condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TripSeats ts JOIN FETCH ts.seatTemplate WHERE ts.id IN :ids")
    List<TripSeats> findByIdInWithLock(@Param("ids") List<Integer> ids);

    List<TripSeats> findByIdIn(List<Integer> ids);

    interface TripSeatMapProjection {
        Integer getTripSeatId();

        Integer getSeatTemplateId();

        String getSeatCode();

        Integer getRowIndex();

        Integer getColIndex();

        String getDeck();

        String getSeatType();

        String getStatus();
    }
}
