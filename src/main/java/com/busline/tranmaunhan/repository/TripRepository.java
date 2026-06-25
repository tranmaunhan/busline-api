package com.busline.tranmaunhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<com.busline.tranmaunhan.entity.Trips, Integer> {

    boolean existsByRouteIdAndVehicleIdAndDepartureTime(Integer routeId, Integer vehicleId, OffsetDateTime departureTime);

    boolean existsByVehicleIdAndDepartureTime(Integer vehicleId, OffsetDateTime departureTime);

    boolean existsByRouteId(Integer routeId);

    @Query(value = """
            SELECT
                trip_id AS tripId,
                departure_time AS departureTime,
                route_origin AS routeOrigin,
                route_destination AS routeDestination,
                license_plate AS licensePlate,
                vehicle_type AS vehicleType
            FROM find_trips_full_route(:pickupLocationId, :dropoffLocationId, :departureDate)
            """, nativeQuery = true)
    List<TripSearchProjection> findTripsFullRoute(
            @Param("pickupLocationId") Integer pickupLocationId,
            @Param("dropoffLocationId") Integer dropoffLocationId,
            @Param("departureDate") LocalDate departureDate);

    @Query(value = """
            SELECT
                trip_id AS tripId,
                departure_time AS departureTime,
                trip_status AS tripStatus,
                vehicle_id AS vehicleId,
                license_plate AS licensePlate,
                vehicle_brand AS vehicleBrand,
                vehicle_type_name AS vehicleTypeName,
                total_seats AS totalSeats,
                route_id AS routeId,
                origin_name AS originName,
                destination_name AS destinationName,
                total_distance_km AS totalDistanceKm,
                total_duration_minutes AS totalDurationMinutes,
                CAST(route_stops AS text) AS routeStops,
                CAST(seat_layout AS text) AS seatLayout
            FROM get_trip_details_v1(:tripId)
            """, nativeQuery = true)
    Optional<TripDetailsProjection> findTripDetails(@Param("tripId") Integer tripId);

    @Query("""
            SELECT
                t.id AS tripId,
                t.route.id AS routeId
            FROM Trips t
            WHERE t.id IN :tripIds
            """)
    List<TripRouteProjection> findRouteIdsByTripIds(@Param("tripIds") Collection<Integer> tripIds);

    @Query("""
            SELECT
                t.id AS tripId,
                t.departureTime AS departureTime,
                t.status AS tripStatus,
                route.id AS routeId,
                origin.id AS originId,
                origin.name AS originName,
                destination.id AS destinationId,
                destination.name AS destinationName,
                vehicle.id AS vehicleId,
                vehicle.licensePlate AS licensePlate,
                vehicle.status AS vehicleStatus,
                vehicleType.typeName AS vehicleTypeName,
                vehicleType.totalSeats AS totalSeats
            FROM Trips t
            JOIN t.route route
            JOIN route.origin origin
            JOIN route.destination destination
            JOIN t.vehicle vehicle
            LEFT JOIN vehicle.vehicleType vehicleType
            WHERE t.departureTime >= :start
              AND t.departureTime < :end
            ORDER BY t.departureTime ASC, t.id ASC
            """)
    List<AdminTripProjection> findAdminTripsByDepartureTimeBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
            SELECT
                t.vehicle.id AS vehicleId,
                MAX(t.departureTime) AS latestDepartureTime
            FROM Trips t
            WHERE t.vehicle.id IN :vehicleIds
            GROUP BY t.vehicle.id
            """)
    List<VehicleLastTripProjection> findLatestTripTimesByVehicleIds(@Param("vehicleIds") Collection<Integer> vehicleIds);

    interface TripSearchProjection {
        Integer getTripId();

        LocalDateTime getDepartureTime();

        String getRouteOrigin();

        String getRouteDestination();

        String getLicensePlate();

        String getVehicleType();
    }

    interface TripRouteProjection {
        Integer getTripId();

        Integer getRouteId();
    }

    interface TripDetailsProjection {
        Integer getTripId();

        LocalDateTime getDepartureTime();

        Integer getTripStatus();

        Integer getVehicleId();

        String getLicensePlate();

        String getVehicleBrand();

        String getVehicleTypeName();

        Integer getTotalSeats();

        Integer getRouteId();

        Integer getOriginId();

        String getOriginName();

        Integer getDestinationId();

        String getDestinationName();


        Double getTotalDistanceKm();

        Integer getTotalDurationMinutes();

        String getRouteStops();

        String getSeatLayout();
    }

    interface AdminTripProjection {
        Integer getTripId();

        OffsetDateTime getDepartureTime();

        Integer getTripStatus();

        Integer getRouteId();

        Integer getOriginId();

        String getOriginName();

        Integer getDestinationId();

        String getDestinationName();

        Integer getVehicleId();

        String getLicensePlate();

        String getVehicleStatus();

        String getVehicleTypeName();

        Integer getTotalSeats();
    }

    interface VehicleLastTripProjection {
        Integer getVehicleId();

        OffsetDateTime getLatestDepartureTime();
    }
}
