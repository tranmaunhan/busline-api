package com.busline.tranmaunhan.dto.trip;

import java.time.OffsetDateTime;
import java.util.List;

public record TripDetailsResponse(
        Integer tripId,
        OffsetDateTime departureTime,
        Integer tripStatus,
        Integer vehicleId,
        String licensePlate,
        String vehicleBrand,
        String vehicleTypeName,
        Integer totalSeats,
        Integer routeId,
        String originName,
        String destinationName,
        Double totalDistanceKm,
        Integer totalDurationMinutes,
        List<String> routeStops,
        List<TripDetailSeatLayoutItemResponse> seatLayout
) {
}
