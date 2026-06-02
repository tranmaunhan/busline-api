package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.RouteSegmentPrices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RouteSegmentPriceRepository extends JpaRepository<RouteSegmentPrices, Integer> {

    @Query("""
            SELECT
                rsp.route.id AS routeId,
                rsp.price AS price
            FROM RouteSegmentPrices rsp
            WHERE rsp.route.id IN :routeIds
              AND rsp.pickupStop.location.id = :pickupLocationId
              AND rsp.dropoffStop.location.id = :dropoffLocationId
            """)
    List<RouteSegmentPriceProjection> findPricesForRoutes(
            @Param("routeIds") Collection<Integer> routeIds,
            @Param("pickupLocationId") Integer pickupLocationId,
            @Param("dropoffLocationId") Integer dropoffLocationId
    );

    @Query("""
            SELECT rsp.price
            FROM RouteSegmentPrices rsp
            WHERE rsp.route.id = :routeId
              AND rsp.pickupStop.location.id = :pickupLocationId
              AND rsp.dropoffStop.location.id = :dropoffLocationId
            """)
    Optional<BigDecimal> findPriceByRouteAndLocations(
            @Param("routeId") Integer routeId,
            @Param("pickupLocationId") Integer pickupLocationId,
            @Param("dropoffLocationId") Integer dropoffLocationId
    );

    interface RouteSegmentPriceProjection {
        Integer getRouteId();

        BigDecimal getPrice();
    }
}
