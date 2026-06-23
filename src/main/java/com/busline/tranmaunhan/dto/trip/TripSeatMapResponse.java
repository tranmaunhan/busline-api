package com.busline.tranmaunhan.dto.trip;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record TripSeatMapResponse(
        Integer tripId,
        OffsetDateTime departureTime,
        String tripStatus,
        Integer routeId,
        String routeOrigin,
        String routeDestination,
        Integer vehicleId,
        String licensePlate,
        String vehicleType,
        Integer totalSeats,
        Integer pickupLocationId,
        Integer dropoffLocationId,
        BigDecimal segmentPrice,
        Integer seatMapSize,
        List<TripSeatMapItemResponse> seats
) {
}
