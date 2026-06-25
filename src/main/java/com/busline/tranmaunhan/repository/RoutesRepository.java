package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Routes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoutesRepository extends JpaRepository<Routes, Integer> {

    @Query("""
            SELECT
                route.id AS routeId,
                origin.name AS originName,
                destination.name AS destinationName,
                route.distanceKm AS distanceKm,
                route.estimatedDurationMinutes AS estimatedDurationMinutes
            FROM Routes route
            JOIN route.origin origin
            JOIN route.destination destination
            ORDER BY origin.name ASC, destination.name ASC, route.id ASC
            """)
    List<AdminRouteProjection> findAdminRoutes();

    interface AdminRouteProjection {
        Integer getRouteId();

        String getOriginName();

        String getDestinationName();

        Double getDistanceKm();

        Integer getEstimatedDurationMinutes();
    }
}
