package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.TripSchedules;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TripScheduleRepository extends JpaRepository<TripSchedules, Integer> {

    @EntityGraph(attributePaths = {"route", "route.origin", "route.destination", "vehicle", "vehicle.vehicleType"})
    List<TripSchedules> findAllByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = {"route", "route.origin", "route.destination", "vehicle", "vehicle.vehicleType"})
    List<TripSchedules> findAllByIdInOrderByCreatedAtDescIdDesc(List<Integer> ids);

    @EntityGraph(attributePaths = {"route", "route.origin", "route.destination", "vehicle", "vehicle.vehicleType"})
    @Query("""
            SELECT ts
            FROM TripSchedules ts
            WHERE ts.status = 1
              AND ts.startDate <= :toDate
              AND (ts.endDate IS NULL OR ts.endDate >= :fromDate)
            ORDER BY ts.departureTime ASC, ts.id ASC
            """)
    List<TripSchedules> findActiveSchedulesForDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
