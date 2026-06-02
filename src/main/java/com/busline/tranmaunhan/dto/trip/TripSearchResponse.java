package com.busline.tranmaunhan.dto.trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripSearchResponse(
        Integer tripId,
        LocalDateTime departureTime,
        String routeOrigin,
        String routeDestination,
        String licensePlate,
        String vehicleType,
        BigDecimal price
) {
}
