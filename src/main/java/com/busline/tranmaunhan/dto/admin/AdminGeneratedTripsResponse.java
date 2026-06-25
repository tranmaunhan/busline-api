package com.busline.tranmaunhan.dto.admin;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminGeneratedTripsResponse(
        LocalDate fromDate,
        LocalDate toDate,
        int schedulesProcessed,
        int tripsCreated,
        int tripsSkipped,
        List<GeneratedTripItem> createdTrips,
        List<String> skippedReasons
) {
    public record GeneratedTripItem(
            Integer scheduleId,
            Integer tripId,
            String routeName,
            String vehicleLabel,
            OffsetDateTime departureTime,
            int generatedSeats
    ) {
    }
}
