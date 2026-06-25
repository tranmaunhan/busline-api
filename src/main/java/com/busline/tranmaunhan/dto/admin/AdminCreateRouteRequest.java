package com.busline.tranmaunhan.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record AdminCreateRouteRequest(
        @NotEmpty(message = "Danh sach diem dung khong duoc rong")
        List<@Valid StopRequest> stops,

        @NotEmpty(message = "Danh sach muc gia khong duoc rong")
        List<@Valid SegmentPriceRequest> segmentPrices,

        @PositiveOrZero(message = "Tong quang duong khong hop le")
        Double distanceKm,

        @PositiveOrZero(message = "Tong thoi gian khong hop le")
        Integer estimatedDurationMinutes
) {
    public record StopRequest(
            @NotNull(message = "locationId khong duoc de trong")
            @Positive(message = "locationId phai > 0")
            Integer locationId,

            @NotNull(message = "distanceFromStartKm khong duoc de trong")
            @PositiveOrZero(message = "distanceFromStartKm phai >= 0")
            Double distanceFromStartKm,

            @NotNull(message = "estimatedTimeFromStartMinutes khong duoc de trong")
            @PositiveOrZero(message = "estimatedTimeFromStartMinutes phai >= 0")
            Integer estimatedTimeFromStartMinutes
    ) {
    }

    public record SegmentPriceRequest(
            @NotNull(message = "pickupStopOrder khong duoc de trong")
            @Positive(message = "pickupStopOrder phai > 0")
            Integer pickupStopOrder,

            @NotNull(message = "dropoffStopOrder khong duoc de trong")
            @Positive(message = "dropoffStopOrder phai > 0")
            Integer dropoffStopOrder,

            @NotNull(message = "price khong duoc de trong")
            @DecimalMin(value = "0.0", inclusive = false, message = "Gia ve phai > 0")
            BigDecimal price
    ) {
    }
}
