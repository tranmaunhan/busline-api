package com.busline.tranmaunhan.dto.trip;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TripSearchResponse(
        Integer tripId,
        OffsetDateTime departureTime,
        OffsetDateTime pickupTime,
        OffsetDateTime dropoffTime,
        String routeOrigin,
        String routeDestination,
        Integer pickupLocationId,
        String pickupLocationName,
        Integer dropoffLocationId,
        String dropoffLocationName,
        String licensePlate,
        String vehicleType,
        Integer availableSeats,
        Integer segmentDurationMinutes,
        BigDecimal price
) {
}
