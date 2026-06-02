package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.RouteStops;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteStopRepository extends JpaRepository<RouteStops, Integer> {

    /**
     * Tìm điểm dừng trên tuyến theo routeId và locationId.
     * Dùng để map pickupLocationId / dropoffLocationId sang RouteStop tương ứng.
     */
    Optional<RouteStops> findByRouteIdAndLocationId(Integer routeId, Integer locationId);
}
