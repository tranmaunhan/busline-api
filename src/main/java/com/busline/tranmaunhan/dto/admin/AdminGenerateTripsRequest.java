package com.busline.tranmaunhan.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record AdminGenerateTripsRequest(
        @NotNull(message = "fromDate khong duoc de trong")
        LocalDate fromDate,

        @NotNull(message = "toDate khong duoc de trong")
        LocalDate toDate,

        List<Integer> scheduleIds
) {
}
