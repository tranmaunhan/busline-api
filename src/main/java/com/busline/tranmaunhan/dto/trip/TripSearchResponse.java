package com.busline.tranmaunhan.dto.trip;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TripSearchResponse(
        Integer tripId,
        OffsetDateTime departureTime,
        String routeOrigin,
        String routeDestination,
        String licensePlate,
        String vehicleType,
        BigDecimal price
) {
}
