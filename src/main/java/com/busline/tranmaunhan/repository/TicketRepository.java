package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Tickets, Integer> {

    @Query("""
            SELECT
                t.trip.id AS tripId,
                COUNT(t.id) AS ticketCount
            FROM Tickets t
            WHERE t.trip.id IN :tripIds
            GROUP BY t.trip.id
            """)
    List<TripTicketCountProjection> countTicketsByTripIds(@Param("tripIds") Collection<Integer> tripIds);

    @Query("""
            SELECT
                route.id AS routeId,
                origin.name AS originName,
                destination.name AS destinationName,
                booking.status AS bookingStatus,
                t.price AS price
            FROM Tickets t
            JOIN t.booking booking
            JOIN t.trip trip
            JOIN trip.route route
            JOIN route.origin origin
            JOIN route.destination destination
            WHERE trip.departureTime >= :start
              AND trip.departureTime < :end
            """)
    List<RouteTicketDetailProjection> findRouteTicketDetailsByDepartureTimeBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    interface TripTicketCountProjection {
        Integer getTripId();

        Long getTicketCount();
    }

    interface RouteTicketDetailProjection {
        Integer getRouteId();

        String getOriginName();

        String getDestinationName();

        Integer getBookingStatus();

        BigDecimal getPrice();
    }
}
