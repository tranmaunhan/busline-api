package com.busline.tranmaunhan.dto.admin;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminTripBookingSeatMapResponse(
        Integer tripId,
        OffsetDateTime departureTime,
        Integer tripStatus,
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
        List<StopOption> stops,
        List<SeatItem> seats
) {
    public record StopOption(
            Integer locationId,
            String locationName,
            Integer stopOrder,
            Integer estimatedTimeFromStartMinutes
    ) {
    }

    public record SeatItem(
            Integer tripSeatId,
            Integer seatTemplateId,
            String seatCode,
            Integer rowIndex,
            Integer colIndex,
            String deck,
            String seatType,
            Integer status,
            Integer bookingId,
            String bookingCode,
            Integer bookingStatus,
            String contactName,
            String contactPhone
    ) {
    }
}
