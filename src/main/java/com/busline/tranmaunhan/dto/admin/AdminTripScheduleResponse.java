package com.busline.tranmaunhan.dto.admin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record AdminTripScheduleResponse(
        Integer id,
        Integer routeId,
        String routeName,
        Integer vehicleId,
        String vehicleLabel,
        LocalTime departureTime,
        LocalDate startDate,
        LocalDate endDate,
        Integer status,
        String statusLabel,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
