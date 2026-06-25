package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.RouteStops;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RouteStopRepository extends JpaRepository<RouteStops, Integer> {

    /**
     * Tìm điểm dừng trên tuyến theo routeId và locationId.
     * Dùng để map pickupLocationId / dropoffLocationId sang RouteStop tương ứng.
     */
    Optional<RouteStops> findByRouteIdAndLocationId(Integer routeId, Integer locationId);

    List<RouteStops> findAllByRouteIdOrderByStopOrderAsc(Integer routeId);

    void deleteAllByRouteId(Integer routeId);

    @Query("""
            SELECT
                rs.route.id AS routeId,
                rs.location.id AS locationId,
                rs.location.name AS locationName,
                rs.stopOrder AS stopOrder,
                rs.estimatedTimeFromStartMinutes AS estimatedTimeFromStartMinutes
            FROM RouteStops rs
            WHERE rs.route.id IN :routeIds
              AND rs.location.id IN :locationIds
            """)
    List<RouteStopTimingProjection> findStopTimingsByRouteIdsAndLocationIds(
            @Param("routeIds") Collection<Integer> routeIds,
            @Param("locationIds") Collection<Integer> locationIds
    );

    interface RouteStopTimingProjection {
        Integer getRouteId();

        Integer getLocationId();

        String getLocationName();

        Integer getStopOrder();

        Integer getEstimatedTimeFromStartMinutes();
    }
}
