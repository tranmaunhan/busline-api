package com.busline.tranmaunhan.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record AdminCreateTripScheduleRequest(
        @NotNull(message = "routeId khong duoc de trong")
        @Positive(message = "routeId phai > 0")
        Integer routeId,

        @NotNull(message = "vehicleId khong duoc de trong")
        @Positive(message = "vehicleId phai > 0")
        Integer vehicleId,

        @NotNull(message = "departureTime khong duoc de trong")
        LocalTime departureTime,

        @NotNull(message = "startDate khong duoc de trong")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "status khong duoc de trong")
        Integer status
) {
}
